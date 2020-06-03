package org.jflame.context.auth.model;

import java.io.Serializable;
import java.util.Set;

/**
 * 基于url的权限的角色
 * 
 * @author yucan.zhang
 */
public interface IRole extends Serializable {

    /**
     * 角色标识
     * 
     * @return
     */
    public String getRoleCode();

    /**
     * 角色权限的url
     * 
     * @return
     */
    public Set<? extends UrlPermission> getPermissions();

    /**
     * 权限验证
     * 
     * @param urlPermission
     * @return
     */
    public boolean isPermitted(String urlPermission);

}
