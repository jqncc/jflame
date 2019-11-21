package org.jflame.commons.common.bean;

import java.text.MessageFormat;

/**
 * api调用返回结果封装类.
 * <p>
 * 返回结果分为3个属性:状态码、结果描述、返回数据。默认结果码为成功200, 默认结果枚举请看:ResultEnum
 * 
 * @see ResultEnum
 * @author zyc
 */
public class CallResult<T> extends SimpleResult {

    private static final long serialVersionUID = 1L;

    private int status = 200; // 状态码
    private String message; // 结果描述
    private T data; // 返回数据

    /**
     * 构造函数,status默认成功
     */
    public CallResult() {
        this(ResultEnum.SUCCESS);
    }

    /**
     * 构造函数,使用默认结果枚举ResultEnum设置status和message
     * 
     * @param result ResultEnum
     */
    public CallResult(ResultEnum result) {
        setResult(result.getStatus(), result.getMessage());
    }

    public CallResult(int status) {
        this.status = status;
    }

    public CallResult(int status, String message) {
        setResult(status, message);
    }

    /**
     * 构造函数
     * 
     * @param status
     * @param message
     * @param data
     */
    public CallResult(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int code) {
        this.status = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CallResult<T> status(int code) {
        this.status = code;
        return this;
    }

    public CallResult<T> error() {
        this.status = ResultEnum.SERVER_ERROR.getStatus();
        return this;
    }

    public CallResult<T> failed() {
        this.status = ResultEnum.FAILED.getStatus();
        return this;
    }

    public CallResult<T> paramError() {
        this.status = ResultEnum.PARAM_ERROR.getStatus();
        return this;
    }

    public CallResult<T> message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置结果数据,并设置状态为成功
     * 
     * @param data
     * @return 返回设置后的CallResult
     */
    public CallResult<T> success(T data) {
        this.status = ResultEnum.SUCCESS.getStatus();
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setResult(BaseResult err) {
        this.status = err.getStatus();
        this.message = err.getMessage();
    }

    public void setResult(int code, String msg) {
        this.status = code;
        this.message = msg;
    }

    public CallResult<T> result(BaseResult err) {
        this.status = err.getStatus();
        this.message = err.getMessage();
        return this;
    }

    /**
     * 结果是否成功,即等于ErrorEnum.SUCCESS.
     * 
     * @return boolean
     */
    public boolean success() {
        return status == ResultEnum.SUCCESS.getStatus();
    }

    public static <T> CallResult<T> ok(T data) {
        return new CallResult<T>().success(data);
    }

    public static <T> CallResult<T> error(String message) {
        CallResult<T> result = new CallResult<>(ResultEnum.SERVER_ERROR.getStatus(), message);
        return result;
    }

    public static <T> CallResult<T> paramError(String message) {
        CallResult<T> result = new CallResult<>(ResultEnum.PARAM_ERROR.getStatus(), message);
        return result;
    }

    /**
     * 默认的执行结果枚举. <br>
     * 200=执行成功 <br>
     * 400=提交的数据错误，即参数错误 <br>
     * 401=身份验证失败,如登录失败等 <br>
     * 403=无权限访问 <br>
     * 500=执行错误,通常是程序执行抛出异常了<br>
     * -1=执行失败,通常是无异常判定为操作失败
     */
    public enum ResultEnum implements BaseResult {
        SUCCESS(200),
        PARAM_ERROR(400),
        NO_AUTH(401),
        FORBIDDEN(403),
        SERVER_ERROR(500),
        FAILED(-1);

        private int status;
        private final static String[] initMsgs = { "执行成功","提交的数据错误","身份验证失败","无权限访问","执行错误","执行失败" };

        private ResultEnum(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        /**
         * 返回结果描述.
         * 
         * @return 结果描述String
         */
        public String getMessage() {
            switch (status) {
                case 200:
                    return initMsgs[0];
                case 400:
                    return initMsgs[1];
                case 401:
                    return initMsgs[2];
                case 403:
                    return initMsgs[3];
                case 500:
                    return initMsgs[4];
                case -1:
                    return initMsgs[5];
                default:
                    break;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format("status={0},message={1},data={2}", status, message,
                (data == null ? "" : data.toString()));
    }

}
