package backend;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import model.Post;
import model.User;
import org.apache.cassandra.exceptions.RequestTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/*
 * For error handling done right see:
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 *
 * Performing stress tests often results in numerous WriteTimeoutExceptions,
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

    private Session session;

    public BackendSession(String contactPoint, String keyspace) throws BackendException {

        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).withRetryPolicy(DefaultRetryPolicy.INSTANCE).build();
        try {
            session = cluster.connect(keyspace);
        } catch (NoHostAvailableException e) {
            throw new BackendException("Could not connect to the database. Please try again later.", e);
        } catch (Exception e) {
            throw new BackendException("Unknown problem occured. Try restarting the application.", e);
        }
        prepareStatements();
    }

    private static PreparedStatement SELECT_USER;
    private static PreparedStatement SELECT_FOLLOWING_USERS;
    private static PreparedStatement SELECT_FOLLOWING;
    private static PreparedStatement SELECT_FOLLOWERS;
    private static PreparedStatement SELECT_FOLLOWER;
    private static PreparedStatement SELECT_POSTS;

    private static PreparedStatement ADD_USER;
    private static PreparedStatement ADD_POST;
    private static PreparedStatement ADD_FOLLOWER;
    private static PreparedStatement ADD_FOLLOWING;

    private static PreparedStatement UPDATE_POST;

    private static PreparedStatement UPDATE_FOLLOWERS;
    private static PreparedStatement UPDATE_FOLLOWING;

    private static PreparedStatement REMOVE_FOLLOWER;
    private static PreparedStatement REMOVE_FOLLOWING;


    private void prepareStatements() throws BackendException {
        try {
            SELECT_USER = session.prepare("SELECT * FROM users WHERE nick = ?");
            SELECT_FOLLOWER = session.prepare("SELECT * FROM followers WHERE nick = ? AND followerNick = ?;");
            SELECT_FOLLOWERS = session.prepare("SELECT * FROM followers WHERE nick = ?;");
            SELECT_FOLLOWING = session.prepare("SELECT * FROM following WHERE nick = ? AND followingNick = ?;");
            SELECT_FOLLOWING_USERS = session.prepare("SELECT * FROM following WHERE nick = ?;");
            SELECT_POSTS = session.prepare("SELECT * FROM posts WHERE authornick=? LIMIT 100;");

            ADD_USER = session.prepare("INSERT INTO users (nick, password, firstName, lastName, birthDate, bio) VALUES (?, ?, ?, ?, ?, ?);");
            ADD_POST = session.prepare("INSERT INTO posts (authornick, creationdate, text) VALUES (?, ?, ?);");
            ADD_FOLLOWER = session.prepare("INSERT INTO followers (nick, followerNick, followerFirstName, followerLastName, followerBirthDate, followerBio) VALUES (?, ?, ?, ?, ?, ?);");
            ADD_FOLLOWING = session.prepare("INSERT INTO following (nick, followingNick, followingFirstName, followingLastName, followingBirthDate, followingBio) VALUES (?, ?, ?, ?, ?, ?);");

            REMOVE_FOLLOWER = session.prepare("DELETE FROM followers WHERE nick=? AND followerNick=?;");
            REMOVE_FOLLOWING = session.prepare("DELETE FROM following WHERE nick=? AND followingNick=?;");

            UPDATE_POST =  session.prepare("UPDATE posts SET text=? WHERE authorNick=? AND creationDate=?");
//            UPDATE_FOLLOWERS = session.prepare("UPDATE followers SET followerFirstName=?, followerLastName=?, followerBirthDate=?, followerBio=? WHERE followerNick=? ALLOW FILTERING;");
//            UPDATE_FOLLOWING = session.prepare("UPDATE following SET followingFirstName=?, followingLastName=?, followingBirthDate=?, followingBio=? WHERE followingNick=? ALLOW FILTERING;");
        } catch (Exception e) {
            throw new BackendException("There is a problem with query preparation. Please contact administrator.", e);
        }

        logger.info("Statements prepared");
    }

    public User selectUser(String nickname) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_USER);
        bs.bind(nickname);

        ResultSet rs = executeStatement(bs, "Could not login to the system.");

        Row record = rs.one();
        if (record == null) return null;
        return new User(nickname, record.getString("password"), record.getString("firstName"),
                record.getString("lastName"), record.getString("birthDate"), record.getString("bio"));
    }

    public ArrayList<String> selectFollowingUsersNicknames(String nick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_FOLLOWING_USERS);
        bs.bind(nick);

        ArrayList<String> followingList = new ArrayList<>();

        ResultSet rs = executeStatement(bs, "Could not fetch feed content.");

        for (Row row : rs) {
            followingList.add(row.getString("followingNick"));
        }

        return followingList;
    }

    public ArrayList<User> selectFollowers(String nick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_FOLLOWERS);
        bs.bind(nick);

        ArrayList<User> followerList = new ArrayList<>();

        ResultSet rs = executeStatement(bs, "Could not fetch follower list.");

        for (Row row : rs) {
            String fNick = row.getString("followerNick");
            String fFirstName = row.getString("followerFirstName");
            String fLastName = row.getString("followerLastName");
            String fBirthDate = row.getString("followerBirthDate");
            String fBio = row.getString("followerBio");
            followerList.add(new User(fNick, "", fFirstName, fLastName, fBirthDate, fBio));
        }

        return followerList;
    }

    public ArrayList<Post> selectPosts(String followingNick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_POSTS);
        bs.bind(followingNick);
//        bs.setList("nicklist", followingNicks);

        ResultSet rs = executeStatement(bs, "Could not fetch recent posts.");

        ArrayList<Post> posts = new ArrayList<>();

        for (Row row : rs) {
            String nick = row.getString("authorNick");
            Date date = row.getTimestamp("creationDate");
            String text = row.getString("text");

            posts.add(new Post(nick, date, text));
        }

        return posts;
    }

    public User selectFollower(String followerNick, String followedNick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_FOLLOWER);
        bs.bind(followedNick, followerNick);

        ResultSet rs = executeStatement(bs, "Could not fetch information about user.");

        Row record = rs.one();
        if (record == null) return null;
        return new User(followerNick, "", record.getString("followerFirstName"),
                record.getString("followerLastName"), record.getString("followerBirthDate"), record.getString("followerBio"));
    }

    public User selectFollowing(String followingNick, String followerNick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_FOLLOWING);
        bs.bind(followerNick, followingNick);

        ResultSet rs = executeStatement(bs, "Could not fetch information about user.");

        Row record = rs.one();
        if (record == null) return null;
        return new User(followingNick, "", record.getString("followingFirstName"),
                record.getString("followingLastName"), record.getString("followingBirthDate"), record.getString("followingBio"));
    }

    public void addUser(String nick, String password, String firstName,
                        String lastName, String birthDate, String bio) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_USER);
        bs.bind(nick, password, firstName, lastName, birthDate, bio);

        executeStatement(bs, "Could not register to the portal.");
    }

    public void addPost(String nick, String content) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_POST);
        bs.bind(nick, new Date(System.currentTimeMillis()), content);

        executeStatement(bs, "Could not add post.");
    }

    public void addFollower(String nick, User followerToAdd) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_FOLLOWER);
        bs.bind(nick, followerToAdd.getNickname(), followerToAdd.getFirstName(), followerToAdd.getLastName(),
                followerToAdd.getBirthDate(), followerToAdd.getBio());

        executeStatement(bs, "Could not follow user.");
    }

    public void addFollowing(String nick, User followedUserToAdd) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_FOLLOWING);
        bs.bind(nick, followedUserToAdd.getNickname(), followedUserToAdd.getFirstName(),
                followedUserToAdd.getLastName(), followedUserToAdd.getBirthDate(), followedUserToAdd.getBio());

        executeStatement(bs, "Could not follow user.");
    }

    public void removeFollower(String nick, String followerToRemoveNick) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_FOLLOWER);
        bs.bind(nick, followerToRemoveNick);

        executeStatement(bs, "Could not unfollow user.");
    }

    public void removeFollowing(String nick, String followedUserToRemoveNick) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_FOLLOWING);
        bs.bind(nick, followedUserToRemoveNick);

        executeStatement(bs, "Could not unfollow user.");
    }

    public void updatePost(Post updatedPost) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_POST);
        bs.bind(updatedPost.getText(), updatedPost.getAuthorNick(), updatedPost.getCreationDate());

        executeStatement(bs, "Could not edit post.");
    }

    public void updateFollowers(User updatedUser) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_FOLLOWERS);
        bs.bind(updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getBirthDate(),
                updatedUser.getBio(), updatedUser.getNickname());

        executeStatement(bs, "Could not edit profile due to database problems. Details: ");
    }

    public void updateFollowing(User updatedUser) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_FOLLOWING);
        bs.bind(updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getBirthDate(),
                updatedUser.getBio(), updatedUser.getNickname());

        executeStatement(bs, "Could not edit profile due to database problems. Details: ");
    }

    private ResultSet executeStatement(BoundStatement stmt, String failMessage) throws BackendException {
        ResultSet rs = null;
        failMessage += "Reason: ";
        try {
            rs = session.execute(stmt);
        } catch (NoHostAvailableException e) {
            failMessage += "database connection lost. Please try restarting the app.";
            throw new BackendException(failMessage, e);
        } catch (RequestTimeoutException e) {
            failMessage += "database operation timed out. Probably server is overcrowded. Please try again later.";
            throw new BackendException(failMessage, e);
        }
        catch (Exception e) {
            failMessage += "unknown error occured. Please contact the application administrator.";
            throw new BackendException(failMessage, e);
        }
        return rs;
    }

    protected void finalize() {
        try {
            if (session != null) {
                session.getCluster().close();
            }
        } catch (Exception e) {
            logger.error("Could not close existing cluster", e);
        }
    }

}

