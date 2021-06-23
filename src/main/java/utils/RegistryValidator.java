package utils;

import backend.BackendException;
import backend.BackendSession;
import model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class RegistryValidator extends Validator {
    SimpleDateFormat dateFormat;


    public RegistryValidator(BackendSession session) {
        super(session);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    public Boolean validateRegistry(String nick, String password, String repeatedPassword, String firstName,
                                    String lastName, String birthDate, String bio) throws BackendException {
        User user = session.selectUser(nick);
        if (validateNick(user, nick) && validatePassword(password, repeatedPassword) && validateName(firstName)
                && validateName(lastName) && validateBirthDate(birthDate) && validateBio(bio)) {
            session.addUser(nick, password, firstName, lastName, birthDate, bio);
            return true;
        }
        return false;
        // TODO return info about error cause, not just boolean
    }

    private boolean validatePassword(String password, String repeatedPassword) {
        return repeatedPassword.equals(password) && password.length() > 3 && password.length() < 100;
    }

    private boolean validateName(String firstName) {
        return firstName.length() > 2 && firstName.length() < 100;
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
