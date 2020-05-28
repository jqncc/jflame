package org.jflame.context.auth.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jflame.commons.util.CollectionHelper;

public class SimpleRole implements IRole {

    private static final long serialVersionUID = 7650007179292176621L;

    protected String roleCode;
    protected Set<UrlPermission> permissions;

    public SimpleRole() {
    }

    public SimpleRole(String roleCode) {
    }

    public SimpleRole(String roleCode, Set<UrlPermission> permissions) {
        this.roleCode = roleCode;
        this.permissions = permissions;
    }

    @Override
    public String getRoleCode() {
        return roleCode;
    }

    @Override
    public Set<UrlPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UrlPermission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(UrlPermission permission) {
        if (permission != null) {
            if (CollectionHelper.isEmpty(permissions)) {
                permissions = new LinkedHashSet<>();
            }
            permissions.add(permission);
        }
    }

    public void addPermissions(Collection<? extends UrlPermission> perms) {
        if (CollectionHelper.isEmpty(perms)) {
            if (permissions == null) {
                permissions = new LinkedHashSet<>(perms.size());
            }
            permissions.addAll(perms);
        }
    }

    public boolean isPermitted(UrlPermission p) {
        Collection<UrlPermission> perms = getPermissions();
        if (CollectionHelper.isEmpty(perms)) {
            for (UrlPermission perm : perms) {
                if (perm.getFunCode()
                        .equals(p.getFunCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPermitted(String urlPermission) {
        Collection<UrlPermission> perms = getPermissions();
        if (CollectionHelper.isEmpty(perms)) {
            for (UrlPermission perm : perms) {
                if (perm.isExistMatchedUrl(urlPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

}
