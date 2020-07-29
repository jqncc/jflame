package org.jflame.context.dubbo;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.jflame.commons.config.PropertiesConfigHolder;
import org.jflame.commons.util.StringHelper;

/**
 * 基于jwt的rest接口验证两种方式
 * 
 * @author yucan.zhang
 */
@PreMatching
public class RestJwtAuthFilter extends BaseTokenAuthFilter {

    private final String jwtKey;

    public RestJwtAuthFilter() {
        super();
        jwtKey = PropertiesConfigHolder.getString("rest.auth.key");
        if (StringHelper.isEmpty(jwtKey)) {
            throw new IllegalStateException("jwt key for 'RestSecurityFilter' not be null");
        }
    }

    @Override
    protected boolean doAuthenticate(String requestUrl, String token, ContainerRequestContext requestContext) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .acceptExpiresAt(60)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            logger.error("jwt验证异常,url:{},ex:{}", requestUrl, ex.getMessage());
            return false;
        }
    }

}
