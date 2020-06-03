package org.jflame.context.auth.context;

/**
 * 将登录用户上下文UserContext绑定到线程,使用了InheritableThreadLocal所以当前线程和子线程都共用
 * 
 * @author yucan.zhang
 */
public final class ThreadLocalUserContextHolderStrategy implements UserContextHolderStrategy {

    private static final InheritableThreadLocal<UserContext> contextHolder = new InheritableThreadLocal<>();

    @Override
    public void clearContext() {
        contextHolder.remove();
    }

    @Override
    public UserContext getContext() {
        UserContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;
    }

    @Override
    public void setContext(UserContext context) {
        if (context == null) {
            throw new IllegalArgumentException("UserContext not be null");
        }
        contextHolder.set(context);
    }

}
