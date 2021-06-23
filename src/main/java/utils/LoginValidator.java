package utils;

import backend.BackendException;
import backend.BackendSession;
import model.User;

public class LoginValidator extends Validator {

    public LoginValidator(BackendSession session) {
        super(session);
    }

    public Boolean validateLogin(String nick, String password) throws BackendException {
        User user = session.selectUser(nick);
        return validateNick(user, nick) && user.getPassword().equals(password);
    }
}
