package utils;

import backend.BackendException;
import backend.BackendSession;
import model.Post;

import java.util.ArrayList;
import java.util.Comparator;

public class FeedProvider {
    BackendSession session;

    public FeedProvider(BackendSession session) {
        this.session = session;
    }

    public ArrayList<Post> getRecentPosts(String nick) throws BackendException {
        ArrayList<String> followingUsersNicknames = session.selectFollowingUsersNicknames(nick);
        ArrayList<Post> recentPosts = new ArrayList<Post>();
        for (String followingNick: followingUsersNicknames) {
            recentPosts.addAll(session.selectPosts(followingNick));
        }
        recentPosts.sort(Comparator.comparing(Post::getCreationDate));
        return recentPosts;
    }
}
