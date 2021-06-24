import backend.BackendException;
import backend.BackendSession;
import cli.Menu;
import utils.LoginValidator;
import utils.RegistryValidator;

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

        //TODO: uncomment when database connected
        BackendSession session = new BackendSession(contactPoint, keyspace);
        Menu menu = new Menu();
        String answer;

        LoginValidator loginValidator = new LoginValidator(session);
        RegistryValidator registryValidator = new RegistryValidator(session);

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
//		String output = session.selectAllPosts();
//		System.out.println("Table contents: \n" + output);
        System.out.println(menu.getPreLoginMenu());
        answer = menu.readAnwser();
        while (!(answer.equals(Menu.LOGIN) || answer.equals(Menu.REGISTER))) {
            System.out.println(menu.getInvalidInput());
            answer = menu.readAnwser();
            //TODO: Ewentualnie exit tutaj jeśli dodamy taką opcję w menu logowania
        }
        // logowanie
        if (answer.equals(Menu.LOGIN)) {

            boolean valid_credentials = false;
            while (!valid_credentials) {
                String[] credentials = menu.readCredentials();
                valid_credentials = loginValidator.validateLogin(credentials[0], credentials[1]);
            }

        } else { //rejestracja

            boolean valid_registration_info = false;
            while (!valid_registration_info) {
                String[] registration_info = menu.readUserForm();
                valid_registration_info = registryValidator.validateRegistry(registration_info[0],
                        registration_info[1], registration_info[2], registration_info[3], registration_info[4],
                        registration_info[5], registration_info[6]);
            }
        }
        System.out.println("Successful login/registration");
        // turbo loop na resztę działania programu xd

        //		session.deleteAll();

        System.exit(0);
    }
}
