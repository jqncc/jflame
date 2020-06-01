package org.jflame.context.dubbo;

import java.lang.reflect.Method;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericService;

import org.jflame.commons.model.BaseResult;

/**
 * dubbo自定义异常处理.
 * <p>
 * 默认异常ExceptionFilter,在捕获到自定义异常时,除非异常与dubbo api服务在同一个jar包否则将包装为RuntimeException抛出,这样客户端无法正确获得自定义异常.<br>
 * <p>
 * 配置示例:
 * 
 * <pre>
 * {@code <dubbo:provider filter="myexception,-exception" />}
 * </pre>
 * 
 * @author yucan.zhang
 *
 */
public class DubboExceptionFilter implements Filter {

    private final Logger logger;
    private final String[] excludePackagePrefixs = { "java.","javax.","com.ghgcn.","org.jflame." };

    public DubboExceptionFilter() {
        this(LoggerFactory.getLogger(DubboExceptionFilter.class));
    }

    public DubboExceptionFilter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                try {
                    Throwable exception = result.getException();

                    // directly throw if it's checked exception
                    if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                        return result;
                    }
                    // directly throw if the exception appears in the signature
                    try {
                        Method method = invoker.getInterface().getMethod(invocation.getMethodName(),
                                invocation.getParameterTypes());
                        Class<?>[] exceptionClassses = method.getExceptionTypes();
                        for (Class<?> exceptionClass : exceptionClassses) {
                            if (exception.getClass().equals(exceptionClass)) {
                                return result;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return result;
                    }

                    // for the exception not found in method's signature, print ERROR message in server's log.
                    logger.error(
                            "Got unchecked and undeclared exception which called by "
                                    + RpcContext.getContext().getRemoteHost() + ". service: "
                                    + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                                    + ", exception: " + exception.getClass().getName() + ": " + exception.getMessage(),
                            exception);

                    // directly throw if exception class and interface class are in the same jar file.
                    String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                    String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                    if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                        return result;
                    }
                    // directly throw if it's JDK exception
                    String className = exception.getClass().getName();
                    if (isExculdeException(className)) {
                        return result;
                    }
                    // directly throw if it's dubbo exception
                    if (exception instanceof RpcException || exception instanceof BaseResult) {
                        return result;
                    }

                    // otherwise, wrap with RuntimeException and throw back to the client
                    return new RpcResult(new RuntimeException(StringUtils.toString(exception)));
                } catch (Throwable e) {
                    logger.warn("Fail to ExceptionFilter when called by " + RpcContext.getContext().getRemoteHost()
                            + ". service: " + invoker.getInterface().getName() + ", method: "
                            + invocation.getMethodName() + ", exception: " + e.getClass().getName() + ": "
                            + e.getMessage(), e);
                    return result;
                }
            }
            return result;
        } catch (RuntimeException e) {
            logger.error("Got unchecked and undeclared exception which called by "
                    + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface().getName()
                    + ", method: " + invocation.getMethodName() + ", exception: " + e.getClass().getName() + ": "
                    + e.getMessage(), e);
            throw e;
        }
    }

    private boolean isExculdeException(String exceptionClassName) {
        for (String pre : excludePackagePrefixs) {
            if (exceptionClassName.startsWith(pre)) {
                return true;
            }
        }
        return false;
    }

}
