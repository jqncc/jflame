package org.jflame.context.auth.model;

import java.io.Serializable;
import java.util.Set;

public interface IRole extends Serializable {

    public String getRoleName();

    public String getRoleCode();

    public String getRootCode();

    public Set<? extends UrlPermission> getPermissions();

}
