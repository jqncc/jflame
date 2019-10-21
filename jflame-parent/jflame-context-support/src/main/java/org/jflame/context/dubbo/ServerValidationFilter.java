package org.jflame.context.dubbo;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.validation.Validation;
import com.alibaba.dubbo.validation.Validator;

/**
 * 参数验证拦截器,用于dubbo服务端替换默认ValidationFilter.dubbo客户端不须替换.
 * <p>
 * 默认Filter抛出验证异常ConstraintViolationException,但ConstraintViolationException没有默认构造函数导致dubbo客户端无法反序列化异常对象.
 * <p>
 * 配置示例:
 * 
 * <pre>
 * {@code <dubbo:provider filter="myvalidation,-validation" />}
 * </pre>
 * 
 * @author yucan.zhang
 *
 */
@Activate(group = { Constants.PROVIDER }, value = Constants.VALIDATION_KEY, order = 9999)
public class ServerValidationFilter implements Filter {

    private Validation validation;

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (validation != null && !invocation.getMethodName().startsWith("$") && ConfigUtils.isNotEmpty(
                invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.VALIDATION_KEY))) {
            try {
                Validator validator = validation.getValidator(invoker.getUrl());
                if (validator != null) {
                    validator.validate(invocation.getMethodName(), invocation.getParameterTypes(),
                            invocation.getArguments());
                }
            } catch (ConstraintViolationException e) {
                // 该异常无默认构造函数,客户端hessian无法反序列化,转为其父类ValidationException
                if (e.getConstraintViolations() != null) {
                    String errMsg = String.join(";", e.getConstraintViolations().stream()
                            .map(ConstraintViolation::getMessage).collect(Collectors.toList()));
                    return new RpcResult(new ValidationException(errMsg));
                } else {
                    return new RpcResult(new ValidationException(e.getMessage()));
                }
            } catch (RpcException e) {
                throw e;
            } catch (Throwable t) {
                return new RpcResult(t);
            }
        }
        return invoker.invoke(invocation);
    }

}
