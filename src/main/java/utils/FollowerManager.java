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

    public boolean isFollowed(User user, User followed) throws BackendException {
        User fromFollowers = session.selectFollower(user.getNickname(), followed.getNickname());
        User fromFollowing = session.selectFollowing(followed.getNickname(), user.getNickname());
        if (fromFollowers != null && fromFollowing != null) return true;
        else if ((fromFollowers == null && fromFollowing == null)) return false;
        throw new BackendException("Couldn't get consistent information about following");
    }

    public void unfollowUser(User user, User userToFollow) throws BackendException {
        session.removeFollower(userToFollow.getNickname(), user.getNickname());
        session.removeFollowing(user.getNickname(), userToFollow.getNickname());
    }
}
