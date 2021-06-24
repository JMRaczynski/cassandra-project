package utils;

import backend.BackendSession;
import model.User;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

abstract class Validator {
    private static final String PROPERTIES_FILENAME = "interface.properties";

    BackendSession session;
    Properties props;

    Validator(BackendSession session) {
        this.session = session;
        props = new Properties();
        try {
            props.load(Objects.requireNonNull(Validator.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME)));
        } catch (IOException ex) {
            System.out.println("There was a problem reading program configuration. Please try again later.");
            System.exit(-1);
        }
    }

    Boolean checkIfUserExists(User user) {
        return user != null;
    }
}
