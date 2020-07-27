package org.jflame.context.auth.context;

import java.util.Optional;

import org.jflame.commons.reflect.SpiFactory;
import org.jflame.context.auth.model.LoginUser;

public class UserContextHolder {

    private static UserContextHolderStrategy strategy;

    static {
        strategy = SpiFactory.getBean(UserContextHolderStrategy.class, ThreadLocalUserContextHolderStrategy.class);
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

    public static Optional<LoginUser> getLoginUser() {
        UserContext ctx = getContext();
        return Optional.ofNullable(ctx.getUser());
    }
}
