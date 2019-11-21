package org.jflame.apidoc.model;

import java.util.Set;

/**
 * 项目描述
 * 
 * @author yucan.zhang
 */
public class ApiProject {

    private String projName;// 项目名
    private int version;// 版本号
    private String desc;// 项目描述
    private String baseUrl;// 接口基础路径
    private Set<ApiModule> apiMdules;// 模块分组

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Set<ApiModule> getApiMdules() {
        return apiMdules;
    }

    public void setApiMdules(Set<ApiModule> apiMdules) {
        this.apiMdules = apiMdules;
    }

}
