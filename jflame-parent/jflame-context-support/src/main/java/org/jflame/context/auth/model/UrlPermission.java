package org.jflame.context.auth.model;

import java.io.Serializable;

/**
 * url权限表示实体类
 * 
 * @author yucan.zhang
 */
public interface UrlPermission extends Serializable {

    public String getFunName();

    public String getFunCode();

    public String[] getFunUrls();

}
