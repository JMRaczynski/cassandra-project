package backend;

import com.datastax.driver.core.*;
import model.Post;
import model.User;
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

        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();
        try {
            session = cluster.connect(keyspace);
        } catch (Exception e) {
            throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
        }
        prepareStatements();
    }

    private static PreparedStatement SELECT_USER;
    private static PreparedStatement SELECT_FOLLOWING_USERS;
    private static PreparedStatement SELECT_POSTS;

    private static PreparedStatement ADD_USER;
    private static PreparedStatement ADD_POST;

    private static PreparedStatement UPDATE_FOLLOWERS;
    private static PreparedStatement UPDATE_FOLLOWING;

//    private static PreparedStatement DELETE_ALL_FROM_USERS;
//    private static PreparedStatement UPDATE_INVARIANT;
//    private static PreparedStatement SELECT_INVARIANT;
//    private static PreparedStatement INCREMENT_URL;

    private static final String USER_FORMAT = "- %-15s  %-15s %-15s %-15s %-15s %-15s\n";

    private void prepareStatements() throws BackendException {
        try {
            SELECT_USER = session.prepare("SELECT * FROM users WHERE nick = ?");
            SELECT_FOLLOWING_USERS = session.prepare("SELECT * FROM following WHERE nick = ?;");
            SELECT_POSTS = session.prepare("SELECT * FROM posts WHERE authornick=? LIMIT 100;");

            ADD_USER = session.prepare("INSERT INTO users (nick, password, firstName, lastName, birthDate, bio) VALUES (?, ?, ?, ?, ?, ?);");
            ADD_POST = session.prepare("INSERT INTO posts (authornick, creationdate, text) VALUES (?, ?, ?);");
//            UPDATE_FOLLOWERS = session.prepare("UPDATE followers SET followerFirstName=?, followerLastName=?, followerBirthDate=?, followerBio=? WHERE followerNick=? ALLOW FILTERING;");
//            UPDATE_FOLLOWING = session.prepare("UPDATE following SET followingFirstName=?, followingLastName=?, followingBirthDate=?, followingBio=? WHERE followingNick=? ALLOW FILTERING;");
//            INSERT_INTO_USERS = session
//                    .prepare("INSERT INTO users (companyName, name, phone, street) VALUES (?, ?, ?, ?);");
//            DELETE_ALL_FROM_USERS = session.prepare("TRUNCATE users;");
//            UPDATE_INVARIANT = session.prepare("UPDATE Invariant SET col1 = ?, col2 = ? WHERE id = 0;");
//            SELECT_INVARIANT = session.prepare("SELECT * FROM invariant WHERE id = 0;");
//            INCREMENT_URL = session.prepare("UPDATE pageviewcounts SET counter = counter + 1, WHERE url = 'counter';");
        } catch (Exception e) {
            throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }

        logger.info("Statements prepared");
    }

    public User selectUser(String nickname) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_USER);
        bs.bind(nickname);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        Row record = rs.one();
        if (record == null) return null;
        return new User(nickname, record.getString("password"), record.getString("firstName"),
                record.getString("lastName"), record.getString("birthDate"), record.getString("bio"));
    }

    public ArrayList<String> selectFollowingUsersNicknames(String nick) throws BackendException {

        BoundStatement bs = new BoundStatement(SELECT_FOLLOWING_USERS);
        bs.bind(nick);

        ArrayList<String> followingList = new ArrayList<>();

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            followingList.add(row.getString("followingNick"));
        }

        return followingList;
    }

    public ArrayList<Post> selectPosts(String followingNick) throws BackendException {
        BoundStatement bs = new BoundStatement(SELECT_POSTS);
        bs.bind(followingNick);
//        bs.setList("nicklist", followingNicks);

        ResultSet rs = null;

        ArrayList<Post> posts = new ArrayList<>();

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not fetch recent posts. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            String nick = row.getString("authorNick");
            Date date = row.getTimestamp("creationDate");
            String text = row.getString("text");

            posts.add(new Post(nick, date, text));
        }

        return posts;
    }

    public void addUser(String nick, String password, String firstName,
                        String lastName, String birthDate, String bio) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_USER);
        bs.bind(nick, password, firstName, lastName, birthDate, bio);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not register to the portal due to database problems. Details: " + e.getMessage() + ".", e);
        }

        //logger.info("model.User " + name + " upserted");
    }

    public void addPost(String nick, String content) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_POST);
        bs.bind(nick, new Date(System.currentTimeMillis()), content);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not add post due to database problems. Details:" + e.getMessage() + ".", e);
        }
    }

    public void updateFollowers(User updatedUser) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_FOLLOWERS);
        bs.bind(updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getBirthDate(),
                updatedUser.getBio(), updatedUser.getNickname());

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not edit profile due to database problems. Details:" + e.getMessage() + ".", e);
        }
    }

    public void updateFollowing(User updatedUser) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_FOLLOWING);
        bs.bind(updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getBirthDate(),
                updatedUser.getBio(), updatedUser.getNickname());

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not edit profile due to database problems. Details:" + e.getMessage() + ".", e);
        }
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

