package org.jflame.context.dubbo;

import org.apache.dubbo.rpc.RpcContext;

import org.jflame.context.auth.context.UserContext;
import org.jflame.context.auth.context.UserContextHolderStrategy;

/**
 * dubbo环境请求用户上下文保存策略实现. UserContext存于RpcContext.ServerContext
 * 
 * @author yucan.zhang
 */
public class DubboUserContextHolderStrategy implements UserContextHolderStrategy {

    public static final String userCtxKey = "dubbo.current.user";

    @Override
    public void clearContext() {
        RpcContext.getServerContext()
                .remove(userCtxKey);
    }

    @Override
    public UserContext getContext() {
        UserContext ctx = (UserContext) RpcContext.getServerContext()
                .get(userCtxKey);
        if (ctx == null) {
            ctx = createEmptyContext();
            setContext(ctx);
        }
        return ctx;
    }

    @Override
    public void setContext(UserContext context) {
        RpcContext.getServerContext()
                .set(userCtxKey, context);
    }

}
