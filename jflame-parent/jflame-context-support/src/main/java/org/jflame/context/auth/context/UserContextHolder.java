package org.jflame.context.auth.context;

import org.jflame.commons.reflect.SpiFactory;

public class UserContextHolder {

    private static UserContextHolderStrategy strategy;

    static {
        strategy = SpiFactory.getBean(UserContextHolderStrategy.class);
        if (strategy == null) {
            strategy = new ThreadLocalUserContextHolderStrategy();
        }
    }

    public static void clearContext() {
        strategy.clearContext();
    }

    public static UserContext getContext() {
        return strategy.getContext();
    }

    public static void setContext(UserContext context) {
        strategy.setContext(context);
    }

    public static UserContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

}
