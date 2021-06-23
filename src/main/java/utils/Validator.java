package utils;

import backend.BackendSession;
import model.User;

abstract class Validator {
    BackendSession session;

    Validator(BackendSession session) {
        this.session = session;
    }

    Boolean validateNick(User user, String nick) {
        return user.getNickname().equals(nick);
    }
}
