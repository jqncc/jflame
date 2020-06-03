package org.jflame.context.auth;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.context.auth.model.UrlPermission;

public final class AuthorityUtils {

    public static boolean hasPermissionByUrl(Set<? extends UrlPermission> userFuns, String checkUrl) {
        boolean hasRight = false;
        if (CollectionHelper.isNotEmpty(userFuns)) {
            for (UrlPermission sysFun : userFuns) {
                // hasRight = isExistMatchedUrl(sysFun.getFunUrls(), checkUrl);
                if (sysFun.isExistMatchedUrl(checkUrl)) {
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
                if (ArrayUtils.contains(funCodes, fun.getCode())) {
                    hasRight = true;
                    break;
                }
            }
        }
        return hasRight;
    }

}
