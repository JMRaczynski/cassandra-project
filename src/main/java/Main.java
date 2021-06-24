import backend.BackendException;
import backend.BackendSession;
import cli.Menu;
import utils.LoginValidator;
import utils.PostCreator;
import utils.UserDataValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Main {

    private static final String PROPERTIES_FILENAME = "config.properties";

    public static void main(String[] args) throws IOException, BackendException {
        String contactPoint = null;
        String keyspace = null;
        String nickname = null;

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
        UserDataValidator userDataValidator = new UserDataValidator(session);
        PostCreator postCreator = new PostCreator(session);

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
                nickname = credentials[0];
                valid_credentials = loginValidator.validateLogin(credentials[0], credentials[1]);
                if (!valid_credentials) {
                    System.out.println(menu.getWrongCredentials());
                }
            }


        } else { //rejestracja

            boolean valid_registration_info = false;
            while (!valid_registration_info) {
                String[] registration_info = menu.readUserForm();
                nickname = registration_info[0];
                valid_registration_info = userDataValidator.validate(registration_info[0],
                        registration_info[1], registration_info[2], registration_info[3], registration_info[4],
                        registration_info[5], registration_info[6], true);
            }
        }
        boolean exit = false;
        while (!exit) {
//            ArrayList<String> fNicks = session.selectFollowingUsersNicknames(nickname);
//            System.out.println(fNicks);
//            System.out.println(session.selectPosts(fNicks));
            System.out.println(menu.getMainMenu());
            String action = menu.readAnwser();
            if (Arrays.asList(Menu.valid_actions).contains(action)) {
                if (action.equals(Menu.EXIT)) {
                    exit = true;
                }

                if (action.equals(Menu.WRITE)) {
                    //TODO: ograniczyć liczbę przyjmowanych znaków
                    String post = menu.processNewPost();
                    if (post != null) {
                        postCreator.createPost(nickname, post);
                    }
                    // TODO: poprawne komunikaty
                    System.out.println("Zapisywanie posta do bazy ale tylko jeśli String post nie jest pusty");
                }

                //TODO: pozostałe akcje
            } else {
                System.out.println(menu.getInvalidInput());
            }
        }

        //		session.deleteAll();

        System.exit(0);
    }
}
