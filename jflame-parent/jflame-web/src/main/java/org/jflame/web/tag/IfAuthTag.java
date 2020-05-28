package org.jflame.web.tag;

import java.util.Set;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.StringHelper;
import org.jflame.context.auth.AuthorityUtils;
import org.jflame.context.auth.model.IRole;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.auth.model.SimpleRole;
import org.jflame.context.auth.model.UrlPermission;
import org.jflame.web.spring.WebContextHolder;

/**
 * 权限判断标签
 * <p>
 * 属性:<br>
 * pm表示权限标识; <br>
 * roles表示角色标识,多个角色可用,隔开;<br>
 * rootRoles表示根角色标识,即根角色下的所有子角色都有权限,多个角色可用,隔开;<br>
 * <p>
 * 1.pm和roles都不设置时，表示所有用户都有权限<br>
 * 2.pm和roles都设置时 条件是or关系,即有一个为true即有权限
 * <p>
 * 使用示例： {@code <jfa:auth pm="SCHOOL_ADD"><a href="#">add school</a></jfa:auth>} <br>
 * 判断结果的存储,在循环语句中需要判断同一权限时，推荐循环外判断一次然后存储结果 :<br>
 * {@code 
 * <jfa:auth var="pm_add" pm="SCHOOL_ADD"><a href="#">add school</a></jfa:auth>}
 * 
 * @author yucan.zhang
 */
public class IfAuthTag extends ConditionalTagSupport {

    private static final long serialVersionUID = 1L;

    private String pm;
    private String roles;

    public String getPm() {
        return pm;
    }

    public void setPm(String pm) {
        this.pm = pm;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    protected boolean condition() throws JspTagException {
        boolean hasPm = false,hasRole = false;
        LoginUser curUser = WebContextHolder.getLoginUser();
        if (StringHelper.isNotEmpty(pm)) {
            Set<UrlPermission> userFuns = curUser.getPermissions();
            hasPm = AuthorityUtils.hasPermissionByFunCode(userFuns, StringHelper.split(pm));
        }
        if (!hasPm) {
            Set<SimpleRole> curRoles = curUser.getRoles();
            if (StringHelper.isNotEmpty(roles)) {
                String[] roleCodes = StringHelper.split(roles);
                for (IRole role : curRoles) {
                    if (ArrayUtils.contains(roleCodes, role.getRoleCode())) {
                        hasRole = true;
                        break;
                    }
                }
            }
        }
        return hasPm || hasRole;
    }

}
