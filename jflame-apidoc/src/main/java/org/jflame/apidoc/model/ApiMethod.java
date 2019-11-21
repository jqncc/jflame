package org.jflame.apidoc.model;

import java.io.Serializable;
import java.util.List;

import org.jflame.apidoc.enums.HttpMethod;
import org.jflame.apidoc.enums.MediaType;

/**
 * api接口方法描述对象
 *
 * @author yucan.zhang
 */
public class ApiMethod implements Serializable {

    private static final long serialVersionUID = -4080727016314277388L;
    /**
     * 接口名
     */
    private String methodName;
    /**
     * 所属模块名
     */
    private String moduleName;
    /**
     * 接口地址
     */
    private String apiUrl;

    /**
     * 接口描述
     */
    private String apiDesc;

    /**
     * 作者
     */
    private String author;
    /**
     * 版本
     */
    private String version;
    /**
     * 请求方式 http method
     */
    private HttpMethod requestMethod;
    /**
     * 请求MIME类型
     */
    private MediaType consumes;
    /**
     * 返回MIME类型
     */
    private MediaType produces;
    /**
     * 查询参数
     */
    private List<ApiParam> queryParams;
    /**
     * body参数
     */
    private List<ApiParam> bodyParams;
    /**
     * header参数
     */
    private List<ApiParam> headerParams;
    /**
     * 接口返回
     */
    private ApiElement result;

    public String getApiName() {
        return methodName;
    }

    public void setApiName(String apiName) {
        this.methodName = apiName;
    }

    public String getApiDesc() {
        return apiDesc;
    }

    public void setApiDesc(String apiDesc) {
        this.apiDesc = apiDesc;
    }

    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public MediaType getConsumes() {
        return consumes;
    }

    public void setConsumes(MediaType consumes) {
        this.consumes = consumes;
    }

    public MediaType getProduces() {
        return produces;
    }

    public void setProduces(MediaType produces) {
        this.produces = produces;
    }

    public List<ApiParam> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<ApiParam> queryParams) {
        this.queryParams = queryParams;
    }

    public List<ApiParam> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(List<ApiParam> bodyParams) {
        this.bodyParams = bodyParams;
    }

    public List<ApiParam> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<ApiParam> headerParams) {
        this.headerParams = headerParams;
    }

    public ApiElement getResult() {
        return result;
    }

    public void setResult(ApiElement result) {
        this.result = result;
    }

}
