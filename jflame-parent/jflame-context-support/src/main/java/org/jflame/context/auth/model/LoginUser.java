package org.jflame.context.auth.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jflame.commons.util.CollectionHelper;

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
    Set<SimpleRole> getRoles();

    /**
     * 判断用户是否有指定url的访问权限
     * 
     * @param urlPermission 要访问的url
     * @return
     */
    default public boolean hasRight(String urlPermission) {
        boolean hasRight = false;
        Set<SimpleRole> roles = getRoles();
        if (CollectionHelper.isNotEmpty(roles)) {
            for (SimpleRole r : roles) {
                hasRight = r.isPermitted(urlPermission);
                if (hasRight) {
                    break;
                }
            }
        }
        return hasRight;
    }

    /**
     * 获取用户的所有权限
     * 
     * @return
     */
    default public Set<UrlPermission> getPermissions() {
        Set<UrlPermission> permissions = new LinkedHashSet<>();
        Set<SimpleRole> roles = getRoles();
        if (CollectionHelper.isNotEmpty(roles)) {
            for (SimpleRole role : roles) {
                if (role.getPermissions() != null) {
                    permissions.addAll(role.getPermissions());
                }
            }
        }
        return permissions;
    }

    /**
     * 判断用户是否有指定角色
     * 
     * @param roleCode 角色标识
     * @return
     */
    default boolean hasRole(String roleCode) {
        boolean hasRole = false;
        Set<SimpleRole> roles = getRoles();
        if (CollectionHelper.isNotEmpty(roles)) {
            hasRole = roles.stream()
                    .anyMatch(p -> p.getRoleCode()
                            .equals(roleCode));
        }
        return hasRole;
    }
}
