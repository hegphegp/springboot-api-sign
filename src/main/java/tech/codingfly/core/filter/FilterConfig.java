package tech.codingfly.core.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SignAuthFilter());
        List<String> urlList = new ArrayList();
        urlList.add("/*");
        registration.setUrlPatterns(urlList);
        registration.setName("signAuthFilter");
        registration.setOrder(1);
        return registration;
    }

}
