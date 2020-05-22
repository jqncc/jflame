package org.jflame.apidoc.model;

import java.io.Serializable;
import java.util.ArrayList;
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
     * api名称最大长度,默认30
     */
    public static final int API_NAME_MAX_LEN = 30;
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

    public void addQueryParam(ApiParam queryParam) {
        if (queryParams == null) {
            queryParams = new ArrayList<>();
        }
        queryParams.add(queryParam);
    }

    public List<ApiParam> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(List<ApiParam> bodyParams) {
        this.bodyParams = bodyParams;
    }

    public void addBodyParam(ApiParam bodyParam) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<>();
        }
        bodyParams.add(bodyParam);
    }

    public List<ApiParam> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<ApiParam> headerParams) {
        this.headerParams = headerParams;
    }

    public void addHeaderParam(ApiParam headerParam) {
        if (headerParams == null) {
            headerParams = new ArrayList<>();
        }
        headerParams.add(headerParam);
    }

    public ApiElement getResult() {
        return result;
    }

    public void setResult(ApiElement result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        char split = ',';
        builder.append("ApiMethod [");
        if (methodName != null) {
            builder.append("methodName=");
            builder.append(methodName);
            builder.append(split);
        }
        if (moduleName != null) {
            builder.append("moduleName=");
            builder.append(moduleName);
            builder.append(split);
        }
        if (apiUrl != null) {
            builder.append("apiUrl=");
            builder.append(apiUrl);
            builder.append(split);
        }
        if (apiDesc != null) {
            builder.append("apiDesc=");
            builder.append(apiDesc);
            builder.append(split);
        }
        if (author != null) {
            builder.append("author=");
            builder.append(author);
            builder.append(split);
        }
        if (version != null) {
            builder.append("version=");
            builder.append(version);
            builder.append(split);
        }
        if (requestMethod != null) {
            builder.append("requestMethod=");
            builder.append(requestMethod);
            builder.append(split);
        }
        if (consumes != null) {
            builder.append("consumes=");
            builder.append(consumes);
            builder.append(split);
        }
        if (produces != null) {
            builder.append("produces=");
            builder.append(produces);
            builder.append(split);
        }
        if (queryParams != null) {
            builder.append("queryParams=");
            builder.append(queryParams);
            builder.append(split);
        }
        if (bodyParams != null) {
            builder.append("bodyParams=");
            builder.append(bodyParams);
            builder.append(split);
        }
        if (headerParams != null) {
            builder.append("headerParams=");
            builder.append(headerParams);
            builder.append(split);
        }
        if (result != null) {
            builder.append("result=");
            builder.append(result);
        }
        builder.append("]");
        return builder.toString();
    }

}
