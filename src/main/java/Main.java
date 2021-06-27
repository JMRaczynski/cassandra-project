import backend.BackendException;
import backend.BackendSession;
import cli.Menu;
import model.Post;
import model.User;
import utils.FeedProvider;
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
        FeedProvider feedProvider = new FeedProvider(session);


//		String output = session.selectAllPosts();
//		System.out.println("Table contents: \n" + output);
        System.out.println(menu.getPreLoginMenu());
        answer = menu.readAnswer();
        while (!(answer.equals(Menu.LOGIN) || answer.equals(Menu.REGISTER))) {
            System.out.println(menu.getInvalidInput());
            answer = menu.readAnswer().toUpperCase();
        }

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


        } else {

            boolean valid_registration_info = false;
            while (!valid_registration_info) {
                String[] registration_info = menu.readUserForm();
                nickname = registration_info[0];
                valid_registration_info = userDataValidator.validate(registration_info[0],
                        registration_info[1], registration_info[2], registration_info[3], registration_info[4],
                        registration_info[5], registration_info[6], true);
                if (!valid_registration_info) {
                    ArrayList<String> messages = userDataValidator.getErrorMessages();
                    for (String message: messages) {
                        System.out.println(message);
                    }
                }
            }
        }

        User loggedUser = session.selectUser(nickname);
        boolean exit = false;
        while (!exit) {

//            System.out.println(feedProvider.getRecentPosts(nickname));
            System.out.println(menu.getMainMenu());
            String action = menu.readAnswer().toUpperCase();
            if (Arrays.asList(Menu.valid_actions).contains(action)) {
                if (action.equals(Menu.EXIT)) {
                    exit = true;
                }

                if (action.equals(Menu.WRITE)) {
                    //TODO: ograniczyć liczbę przyjmowanych znaków
                    String post = menu.processNewPost();
                    if (post != null) {
                        boolean created = postCreator.createPost(nickname, post);
                        if (created) {
                            System.out.println(menu.getPostAdded());
                        }
                    }
                }

                if (action.equals(Menu.S_FOLLOWING)) {
                    System.out.println(menu.getFollowingHeader());
                    ArrayList<String> following = session.selectFollowingUsersNicknames(nickname);
                    for (String name: following) {
                        System.out.println(name);
                    }
                }

                if (action.equals(Menu.S_FOLLOWERS)) {
                    System.out.println(menu.getFollowersHeader());
                    //TODO: backend: get followers
                    ArrayList<String> followers = new ArrayList<>();
                    for (String name: followers) {
                        System.out.println(name);
                    }
                }

                if (action.equals(Menu.POSTS)) {
                    System.out.println(menu.getPostsHeader());
                    ArrayList<Post> posts;
                    posts = feedProvider.getRecentPosts(nickname);
                    for (Post post: posts) {
                        System.out.println(post.getAuthorNick() + " at " + post.getCreationDate() + " posted:");
                        System.out.println(post.getText());
                    }
                }

                if (action.equals(Menu.SEARCH)) {

                    String searchedName = menu.getSearchedName();
                    User searchedUser = session.selectUser(searchedName);
                    if (searchedUser == null) {
                        System.out.println(menu.getNoUserFound());
                    } else {
                        System.out.println(menu.getUserInfoHeader(searchedName));
                        System.out.println(searchedUser.toString());
                        //TODO: follow, unfollow, exit
                    }
                }

                if (action.equals(Menu.EDIT)) {

                }
            } else {
                System.out.println(menu.getInvalidInput());
            }
        }

        //		session.deleteAll();

        System.exit(0);
    }
}
