package org.jflame.context.auth.context;

public class GlobalUserContextHolderStrategy implements UserContextHolderStrategy {

    private static UserContext ctx;

    @Override
    public void clearContext() {
        ctx = null;
    }

    @Override
    public UserContext getContext() {
        if (ctx == null) {
            ctx = createEmptyContext();
        }
        return ctx;
    }

    @Override
    public void setContext(UserContext context) {
        if (context == null) {
            throw new IllegalArgumentException("UserContext not be null");
        }
        ctx = context;
    }

}
