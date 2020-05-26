package org.jflame.context.auth.model;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息接口
 * 
 * @author yucan.zhang
 */
public interface LoginUser extends Serializable {

    /**
     * 用户id
     * 
     * @return
     */
    String getId();

    /**
     * 用户名
     * 
     * @return
     */
    String getUserName();

    /**
     * 用户角色
     * 
     * @return
     */
    Set<? extends IRole> getRoles();

    /**
     * 用户权限
     * 
     * @return
     */
    Set<? extends UrlPermission> getPermissions();
}
