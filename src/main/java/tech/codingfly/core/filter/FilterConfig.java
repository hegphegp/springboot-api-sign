package tech.codingfly.core.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Value("${corsFilter.allowOrigin:*}")
    private String allowOrigin;

    @Value("${corsFilter.allowMethods:DELETE,GET,OPTIONS,POST,PUT,PATCH}")
    private String[] allowMethods;

    @Value("${corsFilter.allowHeaders:*}")
    private String allowHeaders;

    @Value("${corsFilter.maxAge:3600}")
    private long maxAge;

    @Value("${rate-limiter.one-second.limit:600}")
    private Double oneSecondRateLimiter = 600d;
    @Value("${rate-limiter.one-second.one-url.limit:80}")
    private Double oneSecondOneUrlRateLimiter = 80d;
    @Value("${rate-limiter.one-second.one-ip.limit:20}")
    private Double oneSecondOneIpRateLimiter = 20d;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public FilterRegistrationBean<RateLimiterFilter> rateLimiterFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RateLimiterFilter(oneSecondRateLimiter, oneSecondOneUrlRateLimiter, oneSecondOneIpRateLimiter, applicationContext));
        List<String> urlList = new ArrayList() {{ add("/*"); }};
        registration.setUrlPatterns(urlList);
        registration.setName("rateLimiterFilter");
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CorsFilter(allowOrigin, allowMethods, allowHeaders, maxAge));
        List<String> urlList = new ArrayList() {{ add("/*"); }};
        registration.setUrlPatterns(urlList);
        registration.setName("corsFilter");
        registration.setOrder(Integer.MIN_VALUE+1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean signAuthFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SignAuthFilter());
        List<String> urlList = new ArrayList() {{ add("/*"); }};
        registration.setUrlPatterns(urlList);
        registration.setName("signAuthFilter");
        registration.setOrder(Integer.MIN_VALUE+2);
        return registration;
    }

}
