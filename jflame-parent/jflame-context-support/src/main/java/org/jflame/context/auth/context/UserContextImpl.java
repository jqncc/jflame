package org.jflame.context.auth.context;

import org.jflame.context.auth.model.LoginUser;

public class UserContextImpl implements UserContext {

    private static final long serialVersionUID = 1L;

    private LoginUser user;

    public UserContextImpl() {
    }

    public UserContextImpl(LoginUser user) {
        this.user = user;
    }

    @Override
    public LoginUser getUser() {
        return user;
    }

    @Override
    public void setUser(LoginUser curUser) {
        this.user = curUser;
    }

    @Override
    public int hashCode() {
        if (this.user == null) {
            return -1;
        } else {
            return this.user.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof UserContextImpl) {
            UserContextImpl other = (UserContextImpl) obj;
            if (this.getUser() == null && this.getUser() == null) {
                return true;
            }
            if (this.getUser() != null && this.getUser() != null && user.equals(other.user)) {
                return true;
            }
        }

        return false;
    }

}
