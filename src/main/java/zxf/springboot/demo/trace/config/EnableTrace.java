package zxf.springboot.demo.trace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import zxf.springboot.demo.trace.InboundLoggingFilter;
import zxf.springboot.demo.trace.OutboundLoggingInterceptor;
import zxf.springboot.demo.trace.mdc.MDCHelper;
import zxf.springboot.demo.trace.mdc.MDCProperties;
import zxf.springboot.demo.trace.sensitive.SensitiveDataHelper;
import zxf.springboot.demo.trace.sensitive.SensitiveProperties;

@Configuration
public class EnableTrace {
    @Bean
    @Validated
    @ConfigurationProperties(prefix = "zxf.trace.sensitive-mask")
    @ConditionalOnExpression("#{${zxf.trace.inbound.enabled:false} or ${zxf.trace.outbound.enabled:false}}")
    public SensitiveProperties sensitiveProperties() {
        return new SensitiveProperties();
    }

    @Bean
    @ConditionalOnExpression("#{${zxf.trace.inbound.enabled:false} or ${zxf.trace.outbound.enabled:false}}")
    public SensitiveDataHelper sensitiveDataHelper(SensitiveProperties sensitiveProperties) {
        return new SensitiveDataHelper(sensitiveProperties);
    }

    @Bean
    @Validated
    @ConfigurationProperties(prefix = "zxf.trace.inbound.mdc-injection")
    @ConditionalOnProperty(name = "zxf.trace.inbound.enabled", havingValue = "true")
    public MDCProperties mdcProperties() {
        return new MDCProperties();
    }


    @Bean
    @ConditionalOnProperty(name = "zxf.trace.inbound.enabled", havingValue = "true")
    public MDCHelper mdcHelper(MDCProperties mdcProperties) {
        return new MDCHelper(mdcProperties);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnProperty(name = "zxf.trace.inbound.enabled", havingValue = "true")
    public InboundLoggingFilter inboundLoggingFilter(@Value("${zxf.trace.inbound.logging:true}") Boolean logging, MDCHelper mdcHelper, SensitiveDataHelper sensitiveDataHelper) {
        return new InboundLoggingFilter(logging, mdcHelper, sensitiveDataHelper);
    }

    @Bean
    @ConditionalOnProperty(name = "zxf.trace.outbound.enabled", havingValue = "true")
    public OutboundLoggingInterceptor outboundLoggingInterceptor(SensitiveDataHelper sensitiveDataHelper) {
        return new OutboundLoggingInterceptor(sensitiveDataHelper);
    }
}
