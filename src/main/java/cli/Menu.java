package cli;

import java.io.IOException;
import java.sql.SQLOutput;
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

    public String readAnwser() {
        return in.nextLine();
    }

    public String getInvalidInput() {
        return props.getProperty("invalid_input") + "\n";
    }

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
        builder.append(EDIT).append(": ").append(props.getProperty("change_profile")).append("\n");
        return builder.toString();
    }

    public String[] readCredentials() {

        String[] credentials = new String[2];
        System.out.println(props.getProperty("username") + ":");
        credentials[0] = readAnwser();
        System.out.println(props.getProperty("password") + ":");
        credentials[1] = readAnwser();
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
            answers[i] = readAnwser();
        }
        return answers;
    }
}