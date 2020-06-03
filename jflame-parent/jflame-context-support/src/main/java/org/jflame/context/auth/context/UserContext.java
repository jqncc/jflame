package org.jflame.context.auth.context;

import java.io.Serializable;

import org.jflame.context.auth.model.LoginUser;

public interface UserContext extends Serializable {

    LoginUser getUser();

    void setUser(LoginUser curUser);
}
