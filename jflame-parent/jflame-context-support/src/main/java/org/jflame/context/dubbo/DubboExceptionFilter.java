package org.jflame.context.dubbo;

import java.lang.reflect.Method;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.service.GenericService;

import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.config.PropertiesConfigHolder;
import org.jflame.commons.util.ArrayHelper;

/**
 * dubbo自定义异常处理.
 * <p>
 * 默认以下异常直接抛出,其他异常包装为RuntimeException:
 * <ol>
 * <li>抛出的是一个Exception 而非 RuntimeException</li>
 * <li>在方法签名上有声明</li>
 * <li>异常类和接口声明在同一个包里</li>
 * <li>是JDK 自身异常</li>
 * <li>是dubbo 自身异常</li>
 * </ol>
 * <p>
 * 配置示例:
 * 
 * <pre>
 * {@code <dubbo:provider filter="myexception,-exception" />}
 * </pre>
 * 
 * @author yucan.zhang
 */
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter, Filter.Listener {

    private Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);
    private String[] excludePackages;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                Throwable exception = appResponse.getException();

                // directly throw if it's checked exception
                if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                    return;
                }
                // directly throw if the exception appears in the signature
                try {
                    Method method = invoker.getInterface()
                            .getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                    Class<?>[] exceptionClassses = method.getExceptionTypes();
                    for (Class<?> exceptionClass : exceptionClassses) {
                        if (exception.getClass()
                                .equals(exceptionClass)) {
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    return;
                }

                // for the exception not found in method's signature, print ERROR message in server's log.
                logger.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext()
                        .getRemoteHost() + ". service: "
                        + invoker.getInterface()
                                .getName()
                        + ", method: " + invocation.getMethodName() + ", exception: " + exception.getClass()
                                .getName()
                        + ": " + exception.getMessage(), exception);

                // directly throw if exception class and interface class are in the same jar file.
                String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                    return;
                }
                // directly throw if it's JDK exception
                String className = exception.getClass()
                        .getName();
                if (isExculdeException(className)) {
                    return;
                }
                // directly throw if it's dubbo exception
                if (exception instanceof RpcException) {
                    return;
                }

                // otherwise, wrap with RuntimeException and throw back to the client
                appResponse.setException(new RuntimeException(StringUtils.toString(exception)));
            } catch (Throwable e) {
                logger.warn("Fail to ExceptionFilter when called by " + RpcContext.getContext()
                        .getRemoteHost() + ". service: "
                        + invoker.getInterface()
                                .getName()
                        + ", method: " + invocation.getMethodName() + ", exception: " + e.getClass()
                                .getName()
                        + ": " + e.getMessage(), e);
            }

        }
    }

    private boolean isExculdeException(String exceptionClassName) {
        if (excludePackages == null) {
            String[] jdkExPackages = { "java.","javax." };
            String[] exPackages = PropertiesConfigHolder
                    .getStringArray(new ConfigKey<>("dubbo.throw.exception.packages"));
            if (ArrayHelper.isNotEmpty(exPackages)) {
                excludePackages = ArrayHelper.unionArray(exPackages, jdkExPackages);
            } else {
                excludePackages = jdkExPackages;
            }
        }
        for (String pre : excludePackages) {
            if (exceptionClassName.startsWith(pre)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onError(Throwable e, Invoker<?> invoker, Invocation invocation) {
        logger.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext()
                .getRemoteHost() + ". service: "
                + invoker.getInterface()
                        .getName()
                + ", method: " + invocation.getMethodName() + ", exception: " + e.getClass()
                        .getName()
                + ": " + e.getMessage(), e);
    }

    // For test purpose
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
