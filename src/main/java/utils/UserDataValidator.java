package utils;

import backend.BackendException;
import backend.BackendSession;
import lombok.Getter;
import model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class UserDataValidator extends Validator {
    SimpleDateFormat dateFormat;
    @Getter ArrayList<String> errorMessages;


    public UserDataValidator(BackendSession session) {
        super(session);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        errorMessages = new ArrayList<>();
    }


    public Boolean validate(String nick, String password, String repeatedPassword, String firstName,
                                    String lastName, String birthDate, String bio, boolean isNewUser) throws BackendException {
        errorMessages.clear();
        boolean validationPassed = true;
        if (isNewUser) {
            User user = session.selectUser(nick);
            if (checkIfUserExists(user)) {
                errorMessages.add(props.getProperty("duplicated_login"));
                validationPassed = false;
            }
        }
        if (!validatePasswordsMatch(password, repeatedPassword)) {
            errorMessages.add(props.getProperty("passwords_dont_match"));
            validationPassed = false;
        }
        if (!validatePasswordLength(password)) {
            errorMessages.add(props.getProperty("password_wrong_length"));
            validationPassed = false;
        }
        if (!validateName(firstName)) {
            errorMessages.add(props.getProperty("first_name_wrong_length"));
            validationPassed = false;
        }
        if (!validateName(lastName)) {
            errorMessages.add(props.getProperty("last_name_wrong_length"));
            validationPassed = false;
        }
        if (!validateBirthDate(birthDate)) {
            errorMessages.add(props.getProperty("wrong_birth_date"));
            validationPassed = false;
        }
        if (!validateBio(bio)) {
            errorMessages.add(props.getProperty("bio_wrong_length"));
            validationPassed = false;
        }
        if (validationPassed) {
            session.addUser(nick, password, firstName, lastName, birthDate, bio);
        }
//        System.out.println(errorMessages);
        return validationPassed;
    }

    private boolean validatePasswordsMatch(String password, String repeatedPassword) {
        return repeatedPassword.equals(password) && password.length() > 3 && password.length() < 100;
    }

    private boolean validatePasswordLength(String password) {
        return password.length() > 3 && password.length() < 100;
    }

    private boolean validateName(String name) {
        return name.length() > 2 && name.length() < 100;
    }

    private boolean validateBirthDate(String birthDate) {
        try {
            dateFormat.parse(birthDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private boolean validateBio(String bio) {
        return bio.length() < 3000;
    }

}
