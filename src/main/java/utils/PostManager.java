package utils;

import backend.BackendException;
import backend.BackendSession;
import model.Post;

public class PostManager extends Validator {

    public PostManager(BackendSession session) {
        super(session);
    }

    public boolean createPost(String authorNick, String content) throws BackendException {
        if (validatePostLength(content)) {
            session.addPost(authorNick, content);
            return true;
        }
        return false;
    }

    public boolean updatePost(Post post) throws BackendException {
        if (validatePostLength(post.getText())) {
            session.updatePost(post);
            return true;
        }
        return false;
    }

    private boolean validatePostLength(String post) {
        return post.length() > 0 && post.length() < 400;
    }
}
