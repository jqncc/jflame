package org.jflame.commons.common.bean;

import org.jflame.commons.common.bean.CallResult.ResultEnum;

public class SimpleResult implements BaseResult {

    private static final long serialVersionUID = 1L;
    private int status;
    private String message;

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean success() {
        return status == ResultEnum.SUCCESS.getStatus();
    }

    public void setResult(BaseResult result) {
        status = result.getStatus();
        message = result.getMessage();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("status=")
                .append(status)
                .append(", ");
        if (message != null) {
            builder.append("message=")
                    .append(message);
        }
        return builder.toString();
    }

}
