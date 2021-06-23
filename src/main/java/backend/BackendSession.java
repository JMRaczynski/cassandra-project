package backend;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

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
    private static PreparedStatement ADD_USER;
    private static PreparedStatement SELECT_ALL_FROM_USERS;
    private static PreparedStatement SELECT_ALL_FROM_FOLLOWERS;
    private static PreparedStatement SELECT_ALL_FROM_FOLLOWING;
    private static PreparedStatement SELECT_ALL_FROM_POSTS;

//    private static PreparedStatement DELETE_ALL_FROM_USERS;
//    private static PreparedStatement UPDATE_INVARIANT;
//    private static PreparedStatement SELECT_INVARIANT;
//    private static PreparedStatement INCREMENT_URL;

    private static final String USER_FORMAT = "- %-15s  %-15s %-15s %-15s %-15s %-15s\n";
    private static final String POST_FORMAT = "- %-15s  %-15s %-15s\n";
    // private static final SimpleDateFormat df = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void prepareStatements() throws BackendException {
        try {
            SELECT_USER = session.prepare("SELECT * FROM users WHERE nick = '?'");
            SELECT_ALL_FROM_USERS = session.prepare("SELECT * FROM users;");
            SELECT_ALL_FROM_FOLLOWERS = session.prepare("SELECT * FROM followers;");
            SELECT_ALL_FROM_FOLLOWING = session.prepare("SELECT * FROM following;");
            SELECT_ALL_FROM_POSTS = session.prepare("SELECT * FROM posts;");

            ADD_USER = session.prepare("INSERT INTO users (nick, password, firstName, lastName, birthDate, bio) VALUES ('?', '?', '?', '?', '?', '?')");
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

    public String selectAllUsers() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_USERS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            String nick = row.getString("nick");
            String password = row.getString("password");
            String firstName = row.getString("firstName");
            String lastName = row.getString("lastName");
            String birthDate = row.getString("birthDate");
            String bio = row.getString("bio");

            builder.append(String.format(USER_FORMAT, nick, password, firstName, lastName, birthDate, bio));
        }

        return builder.toString();
    }

    public String selectAllFollowers() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_FOLLOWERS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            String nick = row.getString("nick");
            String fNick = row.getString("followerNick");
            String fFirstName = row.getString("followerFirstName");
            String fLastName = row.getString("followerLastName");
            String fBirthDate = row.getString("followerBirthDate");
            String fBio = row.getString("followerBio");

            builder.append(String.format(USER_FORMAT, nick, fNick, fFirstName, fLastName, fBirthDate, fBio));
        }

        return builder.toString();
    }

    public String selectAllFollowing() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_FOLLOWING);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            String nick = row.getString("nick");
            String fNick = row.getString("followingNick");
            String fFirstName = row.getString("followingFirstName");
            String fLastName = row.getString("followingLastName");
            String fBirthDate = row.getString("followingBirthDate");
            String fBio = row.getString("followingBio");

            builder.append(String.format(USER_FORMAT, nick, fNick, fFirstName, fLastName, fBirthDate, fBio));
        }

        return builder.toString();
    }

    public String selectAllPosts() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_POSTS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            String nick = row.getString("authorNick");
            long timestamp = row.getTimestamp("creationDate").getTime();
            String post = row.getString("text");

            builder.append(String.format(POST_FORMAT, nick, timestamp, post));
        }

        return builder.toString();
    }

    public void addUser(String nick, String password, String firstName,
                        String lastName, String birthDate, String bio) throws BackendException {
        BoundStatement bs = new BoundStatement(ADD_USER);
        bs.bind(nick, password, firstName, lastName, birthDate, bio);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        //logger.info("model.User " + name + " upserted");
    }


//
//    public void deleteAll() throws BackendException {
//        BoundStatement bs = new BoundStatement(DELETE_ALL_FROM_USERS);
//
//        try {
//            session.execute(bs);
//        } catch (Exception e) {
//            throw new BackendException("Could not perform a delete operation. " + e.getMessage() + ".", e);
//        }
//
//        logger.info("All users deleted");
//    }
//
//    public void updateInvariant(long threadNum) throws BackendException {
//        BoundStatement bs = new BoundStatement(UPDATE_INVARIANT);
//        bs.bind((int)threadNum, (int)-threadNum);
//
//        try {
//            session.execute(bs);
//        } catch (Exception e) {
//            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
//        }
//
//        logger.info("row upserted successfully");
//    }
//
//    public String selectInvariant() throws BackendException {
//        BoundStatement bs = new BoundStatement(SELECT_INVARIANT);
//
//        ResultSet rs = null;
//
//        try {
//            rs = session.execute(bs);
//        } catch (Exception e) {
//            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
//        }
//
//        String output = "";
//
//        for (Row row : rs) {
//            int positiveThreadNum = row.getInt("col1");
//            int negativeThreadNum = row.getInt("col2");
//            output = "Positive: " + positiveThreadNum + "\nNegative: " + negativeThreadNum;
//        }
//
//        return output;
//    }
//
//    public void incrementCounter() throws BackendException {
//        BoundStatement bs = new BoundStatement(INCREMENT_URL);
//
//        try {
//            session.execute(bs);
//        } catch (Exception e) {
//            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
//        }
//
//    }

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

