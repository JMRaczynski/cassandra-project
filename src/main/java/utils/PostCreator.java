package utils;

import backend.BackendException;
import backend.BackendSession;

public class PostCreator extends Validator {

    public PostCreator(BackendSession session) {
        super(session);
    }

    public boolean createPost(String authorNick, String content) throws BackendException {
        if (validatePostLength(content)) {
            session.addPost(authorNick, content);
            return true;
        }
        return false;
    }

    private boolean validatePostLength(String post) {
        return post.length() > 0 && post.length() < 400;
    }
}
