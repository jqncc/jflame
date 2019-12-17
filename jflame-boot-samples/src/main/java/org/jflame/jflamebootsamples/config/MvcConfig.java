package org.jflame.jflamebootsamples.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.context.env.BaseConfig;
import org.jflame.context.spring.SpringContextHolder;
import org.jflame.context.web.filter.CorsFilter;
import org.jflame.context.web.spring.MyExceptionResolver;

/**
 * spring mvc配置
 * 
 * @author yucan.zhang
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * json组件替换为fastjson
     */
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.BrowserCompatible,
                SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.SkipTransientField);
        converter.setFastJsonConfig(config);
        converter.setSupportedMediaTypes(
                Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN));
        converters.add(0, converter);// 必须设为首位才生效
    }
    /* 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor()) // POS销售管理系统
                .addPathPatterns("/pos/**")
                .excludePathPatterns("/pos/user/login")
                .excludePathPatterns("/pos/user/sms/sendMes")
                .excludePathPatterns("/pos/user/forgot/psd")
                .excludePathPatterns("/pos/businessStatistics/mpQrCode");
    
    }
    
     @Bean
    LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }
    
    
    @Bean
    AuthorityInteceptor authorityInteceptor() {
        return new AuthorityInteceptor();
    }*/

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        MyExceptionResolver myResolver = new MyExceptionResolver();
        resolvers.add(0, myResolver);
    }

    /**
     * 跨域filter
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> addCorsFilter() {
        FilterRegistrationBean<CorsFilter> filterBean = new FilterRegistrationBean<CorsFilter>();
        filterBean.setName("corsFilter");
        filterBean.addUrlPatterns("/*");
        filterBean.addInitParameter("cors.addHeaders", BaseConfig.corsAllowedHeader());
        filterBean.addInitParameter("cors.allowDomains", CollectionHelper.toString(BaseConfig.corsAllowedOrigins()));
        filterBean.setOrder(0);
        filterBean.setFilter(new CorsFilter());
        return filterBean;
    }

    /**
     * RequestContextListener
     * 
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<RequestContextListener> servletListener = new ServletListenerRegistrationBean<>();
        servletListener.setListener(new RequestContextListener());
        return servletListener;
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

}
