package org.jflame.context.auth.model;

import java.util.HashSet;
import java.util.Set;

public class SimpleLoginUser implements LoginUser {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userName;
    private Set<SimpleRole> roles;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRoles(Set<SimpleRole> roles) {
        this.roles = roles;
    }

    @Override
    public Set<? extends IRole> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }

    public void addRole(SimpleRole role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
}
