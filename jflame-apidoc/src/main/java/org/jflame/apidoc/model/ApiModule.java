package org.jflame.apidoc.model;

import java.util.List;

/**
 * 接口模块或分组描述
 * 
 * @author yucan.zhang
 */
public class ApiModule {

    private String moduleName;// 模块名
    private List<ApiMethod> methods;// 模块接口方法

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<ApiMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<ApiMethod> methods) {
        this.methods = methods;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiModule other = (ApiModule) obj;
        if (moduleName == null) {
            if (other.moduleName != null)
                return false;
        } else if (!moduleName.equals(other.moduleName))
            return false;
        return true;
    }

}
