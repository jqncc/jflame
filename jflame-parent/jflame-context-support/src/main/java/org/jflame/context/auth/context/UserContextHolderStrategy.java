package org.jflame.context.auth.context;

public interface UserContextHolderStrategy {

    void clearContext();

    UserContext getContext();

    void setContext(UserContext context);

    default public UserContext createEmptyContext() {
        return new UserContextImpl();
    }
}
