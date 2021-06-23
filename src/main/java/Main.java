import backend.BackendException;
import backend.BackendSession;

import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final String PROPERTIES_FILENAME = "config.properties";

    public static void main(String[] args) throws IOException, BackendException {
        String contactPoint = null;
        String keyspace = null;

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

            contactPoint = properties.getProperty("contact_point");
            keyspace = properties.getProperty("keyspace");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BackendSession session = new BackendSession(contactPoint, keyspace);


//		for (int i = 0; i < 30; i++) {
//			new Thread(() -> {
//				for (int j = 0; j < 1000000; j++) {
//					try {
//						session.updateInvariant(Thread.currentThread().getId());
//						System.out.println(session.selectInvariant());
//					} catch (BackendException e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();
//		}

//		session.upsertUser("PP", "Adam", 609, "A St");
//		session.upsertUser("PP", "Ola", 509, null);
//		session.upsertUser("UAM", "Ewa", 720, "B St");
//		session.upsertUser("PP", "Kasia", 713, "C St");
//
		String output = session.selectAllPosts();
		System.out.println("Table contents: \n" + output);
//
//		session.deleteAll();

        System.exit(0);
    }
}
