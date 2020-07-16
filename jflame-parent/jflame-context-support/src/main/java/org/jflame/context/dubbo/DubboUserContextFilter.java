package org.jflame.context.dubbo;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import org.jflame.commons.config.BaseConfig;
import org.jflame.commons.json.JsonHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.auth.context.UserContext;
import org.jflame.context.auth.context.UserContextHolder;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.auth.model.SimpleLoginUser;

/**
 * dubbo传输过程登录用户信息上下文传递Filter
 * 
 * @author yucan.zhang
 */
@Activate(group = { CommonConstants.PROVIDER,CommonConstants.CONSUMER })
public class DubboUserContextFilter implements Filter {

    private final String[] userFields = { "id","userName" };
    private final String APPNO_KEY = "appNo";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 调用方传递当前用户给服务提供方
        if (RpcContext.getContext()
                .isConsumerSide()) {
            LoginUser curUser = UserContextHolder.getContext()
                    .getUser();
            if (curUser != null) {
                String text = JsonHelper.toJsonInclude(curUser, userFields);
                RpcContext.getContext()
                        .setAttachment(UserContext.CONTEXT_KEY, text);
            }
            String appNo = BaseConfig.getAppNo();
            if (StringHelper.isNotEmpty(BaseConfig.getAppNo())) {
                RpcContext.getContext()
                        .setAttachment(APPNO_KEY, appNo);
            }
        } else if (RpcContext.getContext()
                .isProviderSide()) {
            // 服务提供方获取当前传递过来的用户
            String userText = RpcContext.getContext()
                    .getAttachment(UserContext.CONTEXT_KEY);
            if (StringHelper.isNotEmpty(userText)) {
                SimpleLoginUser user = JsonHelper.parseObject(userText, SimpleLoginUser.class);
                UserContextHolder.getContext()
                        .setUser(user);
                RpcContext.getContext()
                        .removeAttachment(UserContext.CONTEXT_KEY);
            }
        }
        return invoker.invoke(invocation);
    }

}
