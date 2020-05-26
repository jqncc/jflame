package org.jflame.context.auth;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlMatcher;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.auth.model.UrlPermission;

public final class AuthorityUtils {

    public static boolean hasPermissionByUrl(Set<? extends UrlPermission> userFuns, String checkUrl) {
        boolean hasRight = false;
        if (CollectionHelper.isNotEmpty(userFuns)) {
            for (UrlPermission sysFun : userFuns) {
                hasRight = isExistMatchedUrl(sysFun.getFunUrls(), checkUrl);
                if (hasRight) {
                    break;
                }
            }
        }
        return hasRight;
    }

    public static boolean hasPermissionByFunCode(Set<? extends UrlPermission> userFuns, String... funCodes) {
        boolean hasRight = false;
        if (CollectionHelper.isNotEmpty(userFuns)) {
            for (UrlPermission fun : userFuns) {
                if (ArrayUtils.contains(funCodes, fun.getFunCode())) {
                    hasRight = true;
                    break;
                }
            }
        }
        return hasRight;
    }

    /**
     * 判断登录用户是否有指定的权限
     * 
     * @param loginUser 登录用户
     * @param funCode 权限标识
     * @return
     */
    public static boolean hasPermission(LoginUser loginUser, String funCode) {
        return hasPermissionByFunCode(loginUser.getPermissions(), funCode);
    }

    /**
     * 判断登录用户是否是指定的角色
     * 
     * @param loginUser 登录用户
     * @param roleCode 角色标识
     * @return
     */
    public static boolean hasRole(LoginUser loginUser, String roleCode) {
        boolean hasRole = false;
        if (CollectionHelper.isNotEmpty(loginUser.getRoles())) {
            hasRole = loginUser.getRoles()
                    .stream()
                    .anyMatch(p -> p.getRoleCode()
                            .equals(roleCode));
        }
        return hasRole;
    }

    private static boolean isExistMatchedUrl(String[] funUrls, String checkUrl) {
        boolean isMatched = false;
        if (ArrayUtils.isNotEmpty(funUrls)) {
            for (String funUrl : funUrls) {
                isMatched = matchUrl(funUrl, checkUrl);
                if (isMatched) {
                    isMatched = true;
                    break;
                }
            }
        }
        return isMatched;
    }

    private static boolean matchUrl(String pattern, String url) {
        boolean isMatched = false;
        final String urlSpit = "/";
        if (pattern.endsWith(urlSpit)) {
            pattern = StringHelper.removeLast(pattern);
        }
        if (url.endsWith(urlSpit)) {
            url = StringHelper.removeLast(url);
        }
        if (pattern.equals(url)) {
            isMatched = true;
        } else {
            isMatched = UrlMatcher.match(pattern, url);
            // 匹配 /sys/view/{id} 类似rest风格url
            /*int firstAsterisk = pattern.indexOf("/*");
            if (firstAsterisk >= 0) {
                final String star = "*";
                if (url.startsWith(pattern.substring(0, firstAsterisk))) {
                    String[] pnArr = pattern.split(urlSpit);
                    String[] urlArr = url.split(urlSpit);
                    if (pnArr.length == urlArr.length) {
                        boolean tmp = true;
                        for (int i = 0; i < pnArr.length; i++) {
                            if (!star.equals(pnArr[i]) && !pnArr[i].equals(urlArr[i])) {
                                tmp = false;
                                break;
                            }
                        }
                        if (tmp) {
                            isMatched = true;
                        }
                    }
                }
            }*/
        }
        return isMatched;
    }

}
