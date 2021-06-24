package utils;

import backend.BackendException;
import backend.BackendSession;
import model.User;

public class FollowerManager {
    BackendSession session;

    public FollowerManager(BackendSession session) {
        this.session = session;
    }

    public void followUser(User user, User userToFollow) throws BackendException {
        session.addFollower(userToFollow.getNickname(), user);
        session.addFollowing(user.getNickname(), userToFollow);
    }

    public void unfollowUser(User user, User userToFollow) throws BackendException {
        session.removeFollower(userToFollow.getNickname(), user.getNickname());
        session.removeFollowing(user.getNickname(), userToFollow.getNickname());
    }
}
