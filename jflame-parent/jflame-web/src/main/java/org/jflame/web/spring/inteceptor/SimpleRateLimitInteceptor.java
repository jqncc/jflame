package org.jflame.web.spring.inteceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.util.concurrent.RateLimiter;

import org.jflame.commons.model.CallResult;
import org.jflame.web.WebUtils;
import org.jflame.web.spring.SpringWebUtils;

/**
 * 简易的单机接口请求速率限制拦截器.
 * 
 * @author yucan.zhang
 */
public class SimpleRateLimitInteceptor implements HandlerInterceptor {

    private static Map<String,RateLimiter> limiterMap = new ConcurrentHashMap<>();
    private final CallResult<?> result = new CallResult<>(HttpServletResponse.SC_GATEWAY_TIMEOUT, "请求太频率,请稍候再试");
    private Map<String,Double> urlRateMap = new HashMap<>();
    private double defaultRate;

    public SimpleRateLimitInteceptor(double permitsPerSecond) {
        defaultRate = permitsPerSecond;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = WebUtils.getRequestPath(request);
        RateLimiter limiter = getRateLimiter(uri);

        if (!limiter.tryAcquire()) {
            if (SpringWebUtils.isJsonResult(request, handler)) {
                WebUtils.outJson(response, result);
            } else {
                response.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT, "请求太频率,请稍候再试");
            }
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    private RateLimiter getRateLimiter(String apiUrl) {
        RateLimiter limiter = limiterMap.get(apiUrl);
        if (limiter == null) {
            limiter = RateLimiter.create(urlRateMap.getOrDefault(apiUrl, defaultRate));
            limiterMap.put(apiUrl, limiter);
        }
        return limiter;
    }

    /**
     * 单独设置接口的限制速率
     * 
     * @param urlRateMap
     */
    public void setUrlRateMap(Map<String,Double> urlRateMap) {
        this.urlRateMap = urlRateMap;
    }

}
