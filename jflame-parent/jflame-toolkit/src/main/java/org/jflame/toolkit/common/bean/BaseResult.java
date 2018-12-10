package org.jflame.toolkit.common.bean;

import java.io.Serializable;

import org.jflame.toolkit.exception.BusinessException;

public interface BaseResult extends Serializable {

    public int getStatus();

    public String getMessage();

    /**
     * 使用错误编码和描述生成一个通用业务异常
     * 
     * @return
     */
    default public BusinessException toExcept() {
        return new BusinessException(this);
    }

    @SuppressWarnings("rawtypes")
    default public CallResult createCallResult() {
        return new CallResult(getStatus(), getMessage());
    }

    default public void setCallResult(CallResult<?> result) {
        result.setResult(getStatus(), getMessage());
    }
}
