package org.jflame.context.auth.model;

import java.io.Serializable;
import java.util.Set;

/**
 * 基于url的权限的角色
 * 
 * @author yucan.zhang
 */
public interface IRole extends Serializable {

    public String getRoleCode();

    public Set<UrlPermission> getPermissions();

}
