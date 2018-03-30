package edu.mit.cci.pogs.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<ResourceUrlEncodingFilter> resourceEncodingFilter() {
        FilterRegistrationBean<ResourceUrlEncodingFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new ResourceUrlEncodingFilter());
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new ShallowEtagHeaderFilter());
        return registrationBean;
    }
}
