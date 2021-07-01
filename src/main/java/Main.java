import backend.BackendException;
import backend.BackendSession;
import cli.Menu;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import model.Post;
import model.User;
import org.apache.cassandra.exceptions.RequestTimeoutException;
import org.apache.tools.ant.taskdefs.Sleep;
import utils.*;

import java.io.IOException;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class Main {

    private static final String PROPERTIES_FILENAME = "config.properties";

    public static void main(String[] args) {
        String contactPoint = null;
        String keyspace = null;
        String nickname = null;
        Menu menu = new Menu();
        String answer;
        BackendSession session = null;

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

            contactPoint = properties.getProperty("contact_point");
            keyspace = properties.getProperty("keyspace");
        } catch (IOException ex) {
            handleException(ex);
        }

        try {
            session = new BackendSession(contactPoint, keyspace);
        } catch (BackendException ex) {
            handleException(ex);
        }

        LoginValidator loginValidator = new LoginValidator(session);
        UserDataValidator userDataValidator = new UserDataValidator(session);
        PostManager postManager = new PostManager(session);
        FeedProvider feedProvider = new FeedProvider(session);
        FollowerManager followerManager = new FollowerManager(session);

//		String output = session.selectAllPosts();
//		System.out.println("Table contents: \n" + output);

        System.out.println(menu.getPreLoginMenu());
        answer = menu.readAnswer().toUpperCase();
        while (!(answer.equals(Menu.LOGIN) || answer.equals(Menu.REGISTER))) {
            System.out.println(menu.getInvalidInput());
            answer = menu.readAnswer().toUpperCase();
        }

        if (answer.equals(Menu.LOGIN)) {

            boolean valid_credentials = false;
            while (!valid_credentials) {
                String[] credentials = menu.readCredentials();
                nickname = credentials[0];
                try{
                    valid_credentials = loginValidator.validateLogin(credentials[0], credentials[1]);
                } catch (Exception ex) {
                    handleException(ex);
                }
                if (!valid_credentials) {
                    System.out.println(menu.getWrongCredentials());
                }
            }


        } else {

            boolean valid_registration_info = false;
            while (!valid_registration_info) {
                String[] registration_info = menu.readUserForm();
                nickname = registration_info[0];
                try {
                    valid_registration_info = userDataValidator.validate(registration_info[0],
                            registration_info[1], registration_info[2], registration_info[3], registration_info[4],
                            registration_info[5], registration_info[6], true);
                } catch (Exception ex) {
                    handleException(ex);
                }
                if (!valid_registration_info) {
                    ArrayList<String> messages = userDataValidator.getErrorMessages();
                    for (String message: messages) {
                        System.out.println(message);
                    }
                }
            }
        }

        User loggedUser = null;
        try {
            loggedUser = session.selectUser(nickname);
        } catch (Exception ex) {
            handleException(ex);
        }
        boolean exit = false;
        while (!exit) {

            System.out.println(menu.getMainMenu());
            String action = menu.readAnswer().toUpperCase();
            if (Arrays.asList(Menu.valid_actions).contains(action)) {
                if (action.equals(Menu.EXIT)) {
                    exit = true;
                }

                if (action.equals(Menu.STRESS_SELECT)) {
                    for (int i = 0; i < 16; i++) {
                        String finalNickname = nickname;
                        new Thread(() -> {
                            for (int j = 0; j < 100000; j++) {
                                try {
                                    feedProvider.getRecentPosts(finalNickname);
                                } catch (BackendException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                     }
                }

                if (action.equals(Menu.STRESS_UPSERT)) {
                    System.out.println(menu.getSpamStart());
                    for (int i = 0; i < 1000; i++) {
                        try {
                            session.addPost(nickname, null);
                        } catch (BackendException e) {
                            handleException(e);
                        }
                    }
                    System.out.println(menu.getSpamEnd());
                }

                if (action.equals(Menu.WRITE)) {
                    String post = menu.processNewPost();
                    if (post != null) {
                        boolean created = false;
                        try {
                            postManager.createPost(nickname, post);
                        } catch (Exception ex) {
                            handleException(ex);
                        }
                        if (created) {
                            System.out.println(menu.getPostAdded());
                        }
                    } else {
                        System.out.println(menu.getEmptyPost());
                    }
                }

                if (action.equals(Menu.S_FOLLOWING)) {
                    System.out.println(menu.getFollowingHeader());
                    try {
                        ArrayList<String> following = session.selectFollowingUsersNicknames(nickname);
                        for (String name : following) {
                            System.out.println(name);
                        }
                    } catch (Exception ex) {
                        handleException(ex);
                    }
                }

                if (action.equals(Menu.S_FOLLOWERS)) {
                    System.out.println(menu.getFollowersHeader());
                    try {
                        ArrayList<User> followers = session.selectFollowers(nickname);
                        for (User user : followers) {
                            System.out.println(user.getNickname());
                        }
                    } catch (Exception ex) {
                        handleException(ex);
                    }
                }

                if (action.equals(Menu.POSTS)) {
                    System.out.println(menu.getPostsHeader());
                    ArrayList<Post> posts;
                    try {
                        posts = feedProvider.getRecentPosts(nickname);
                        for (Post post : posts) {
                            System.out.println(post.getAuthorNick() + " at " + post.getCreationDate() + " posted:");
                            System.out.println(post.getText());
                            System.out.println("\n\t\t\t***\t\t\t***\t\t\t\n");
                        }
                    } catch (Exception ex) {
                        handleException(ex);
                    }
                }

                if (action.equals(Menu.SEARCH)) {

                    String searchedName = menu.getSearchedName();
                    User searchedUser = null;
                    try {
                        searchedUser = session.selectUser(searchedName);
                    } catch (Exception ex) {
                        handleException(ex);
                    }
                    if (searchedUser == null) {
                        System.out.println(menu.getNoUserFound());
                    } else {
                        System.out.println(menu.getUserInfoHeader(searchedName));
                        System.out.println(searchedUser.toString());

                        boolean follow = false;
                        boolean unfollow = false;
                        try {
                            unfollow = followerManager.isFollowed(loggedUser, searchedUser);
                            if (!unfollow) {
                                follow = true;
                            }
                        } catch (BackendException exception) {
                            System.out.println(exception.getMessage());
                        }
                        boolean valid = false;
                        while (!valid) {
                            System.out.println(menu.getUserMenu(follow, unfollow));
                            String userAction = menu.readAnswer().toUpperCase();
                            if (!(userAction.equals(Menu.FOLLOW) || userAction.equals(Menu.UNFOLLOW) || userAction.equals(Menu.EXIT))) {
                                System.out.println(menu.getInvalidInput());
                            } else {
                                try {
                                    if (userAction.equals(Menu.FOLLOW)) {
                                        followerManager.followUser(loggedUser, searchedUser);
                                        System.out.println("Now you are following " + searchedName);
                                    } else if (userAction.equals(Menu.UNFOLLOW)) {
                                        followerManager.unfollowUser(loggedUser, searchedUser);
                                        System.out.println("You are no longer following " + searchedName);
                                    }
                                } catch (Exception ex) {
                                    handleException(ex);
                                }
                                valid = true;
                            }
                        }
                    }
                }

                if (action.equals(Menu.EDIT)) {
                    try {
                        ArrayList<Post> posts = session.selectPosts(nickname);
                        Integer postNumber = menu.getPostNumber(posts);
                        String text = menu.processPostEdit(posts.get(postNumber));
                        if (text != null) {
                            Post editedPost = posts.get(postNumber);
                            editedPost.setText(text);
                            boolean edited = postManager.updatePost(editedPost);
                            if (edited) {
                                System.out.println(menu.getPostEdited());
                            }
                        } else {
                            System.out.println(menu.getEmptyPost());
                        }
                    } catch (Exception ex) {
                        handleException(ex);
                    }
                }
            } else {
                System.out.println(menu.getInvalidInput());
            }
        }

        System.exit(0);
    }

    private static void handleException(Exception e) {
        if (e.getCause().getClass().equals(NoHostAvailableException.class)) {
            System.out.println(e.getMessage());
            System.exit(1);
        } else {
            System.out.println(e.getMessage());
//            if (!e.getCause().getClass().equals(RequestTimeoutException.class)) {
//                e.printStackTrace();
//            }
        }
    }
}
