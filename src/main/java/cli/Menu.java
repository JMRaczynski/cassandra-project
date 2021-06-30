package cli;

import model.Post;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class Menu {

    public static final String LOGIN = "L";
    public static final String REGISTER="R";
    public static final String S_FOLLOWERS="F";
    public static final String S_FOLLOWING="G";
    public static final String SEARCH="S";
    public static final String POSTS="P";
    public static final String WRITE="W";
    public static final String EDIT="E";
    public static final String EXIT="X";
    public static final String YES="Y";
    public static final String NO="N";
    public static final String FOLLOW="F";
    public static final String UNFOLLOW="U";
    public static final String STRESS_SELECT="SPAM";
    public static final String STRESS_UPSERT="SPAM2";
    public static final String[] valid_actions = {S_FOLLOWERS, S_FOLLOWING, SEARCH, POSTS, WRITE, EDIT, EXIT, STRESS_SELECT, STRESS_UPSERT};


    private static final String PROPERTIES_FILENAME = "interface.properties";
    Properties props = new Properties();
    Scanner in = new Scanner(System.in);

    public Menu() {
        try {
            props.load(Objects.requireNonNull(Menu.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String readAnswer() {
        return in.nextLine();
    }

    public String getInvalidInput() {
        return props.getProperty("invalid_input");
    }

    public String getWrongCredentials() {
        return props.getProperty("login_failed");
    }

    public String getPostAdded() { return props.getProperty("post_added"); }

    public String getPostEdited() { return props.getProperty("post_edited"); }

    public String getFollowingHeader() { return props.getProperty("following_header") + ":"; }

    public String getFollowersHeader() { return props.getProperty("followers_header") + ":"; }

    public String getPostsHeader() { return props.getProperty("posts_header") + ":"; }

    public String getNoUserFound() { return props.getProperty("no_user_found") + ":"; }

    public String getSearchedName() {
        System.out.println(props.getProperty("search_for"));
        return readAnswer();
    }

    public String getUserInfoHeader(String nickname) { return props.getProperty("user_info") + " " + nickname; }

    public String getPreLoginMenu() {

        StringBuilder builder = new StringBuilder();
        builder.append(props.getProperty("greeting_new")).append("\n");
        builder.append(LOGIN).append(": ").append(props.getProperty("log_in")).append("\n");
        builder.append(REGISTER).append(": ").append(props.getProperty("register"));
        return builder.toString();
    }

    public String getMainMenu() {

        StringBuilder builder = new StringBuilder();
        builder.append(props.getProperty("select")).append(":\n");
        builder.append(WRITE).append(": ").append(props.getProperty("post")).append("\n");
        builder.append(S_FOLLOWERS).append(": ").append(props.getProperty("show_followers")).append("\n");
        builder.append(S_FOLLOWING).append(": ").append(props.getProperty("show_following")).append("\n");
        builder.append(POSTS).append(": ").append(props.getProperty("show_posts")).append("\n");
        builder.append(SEARCH).append(": ").append(props.getProperty("search_for_users")).append("\n");
        builder.append(EDIT).append(": ").append(props.getProperty("edit_post")).append("\n");
        builder.append(EXIT).append(": ").append(props.getProperty("exit"));
        return builder.toString();
    }

    public String[] readCredentials() {

        String[] credentials = new String[2];
        System.out.println(props.getProperty("username") + ":");
        credentials[0] = readAnswer();
        System.out.println(props.getProperty("password") + ":");
        credentials[1] = readAnswer();
        return credentials;
    }

    public String[] getUserForm() {

        String[] entrynames = {props.getProperty("username"), props.getProperty("password"), props.getProperty("rewrite_password"),
                props.getProperty("first_name"), props.getProperty("last_name"), props.getProperty("birthdate"), props.getProperty("bio")};
        for (int i=0; i<entrynames.length; i++) {
            entrynames[i] += ":";
        }
        return entrynames;
    }

    public String[] readUserForm() {

        String[] entrynames = getUserForm();
        String[] answers = new String[7];
        System.out.println(props.getProperty("fill_in_registration") + "\n");
        for (int i=0; i<entrynames.length; i++) {
            System.out.println(entrynames[i]);
            answers[i] = readAnswer();
        }
        return answers;
    }

    public String processNewPost() {

        System.out.println(props.getProperty("writing_information"));
        String post = readAnswer();
        String answer = "F";
        while (!(answer.equals(YES) || answer.equals(NO))) {
            System.out.println(props.getProperty("publish"));
            answer = readAnswer();
        }
        if (answer.equals(YES)) {
            return post;
        } else {
            return null;
        }
    }

    public String getUserMenu(boolean follow, boolean unfollow) {
        StringBuilder builder = new StringBuilder();
        builder.append(props.getProperty("available_actions")).append(":\n");
        if (follow) {
            builder.append(FOLLOW).append(": ").append(props.getProperty("follow")).append("\n");
        }
        if (unfollow) {
            builder.append(UNFOLLOW).append(": ").append(props.getProperty("unfollow")).append("\n");
        }
        builder.append(EXIT).append(": ").append(props.getProperty("exit"));
        return builder.toString();
    }

    public Integer getPostNumber(ArrayList<Post> posts) {
        for (int i=0; i<posts.size(); i++) {
            System.out.println(i+1 + ": " + posts.get(i).getText());
            System.out.println("\n\t\t\t***\t\t\t***\t\t\t\n");
        }
        boolean valid = false;
        Integer number = null;
        while (!valid) {
            System.out.println(props.getProperty("select_post_to_edit"));
            String answer = readAnswer();
            try {
                number = Integer.valueOf(answer);
                if (number < 1 || number > posts.size()) {
                    System.out.println(props.getProperty("invalid_number"));
                } else {
                    valid = true;
                }
            } catch (Exception e) {
                System.out.println(props.getProperty("invalid_number"));
            }
        }
        return number-1;
    }

    public String processPostEdit(Post post) {
        System.out.println(props.getProperty("edit_header"));
        System.out.println(post.getText());
        System.out.println(props.getProperty("edit_information"));
        String edited = readAnswer();
        String answer = "F";
        while (!(answer.equals(YES) || answer.equals(NO))) {
            System.out.println(props.getProperty("save_changes"));
            answer = readAnswer();
        }
        if (answer.equals(YES)) {
            return edited;
        } else {
            return null;
        }
    }
}