package org.zalando.nakadi.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;

@Configuration
@EnableMetrics
public class MetricsConfig {
    @Bean
    public ServletRegistrationBean servletRegistrationBean(final MetricRegistry metricRegistry) {
        return new ServletRegistrationBean(new MetricsServlet(metricRegistry), "/metrics/*");
    }

    class SubscriptionMetricsServlet extends MetricsServlet {
        public SubscriptionMetricsServlet(final MetricRegistry metricRegistry) {
            super(metricRegistry);
        }
    }

    class KafkaClientsMetricsServlet extends MetricsServlet {
        public KafkaClientsMetricsServlet (final MetricRegistry metricRegistry) {
            super(metricRegistry);
        }
    }

    @Bean
    public ServletRegistrationBean subscriptionsServletRegistrationBean(
            @Qualifier("perPathMetricRegistry") final MetricRegistry metricRegistry) {
        return new ServletRegistrationBean(new SubscriptionMetricsServlet(metricRegistry), "/request-metrics/*");
    }

    @Bean
    public ServletRegistrationBean kafkaClientsMetricsServletRegistrationBean(
            @Qualifier("kafkaClientsMetricRegistry") final MetricRegistry metricRegistry) {
        return new ServletRegistrationBean(new KafkaClientsMetricsServlet(metricRegistry), "/kafka-clients-metrics/*");
    }

    @Bean
    public MetricsConfigurerAdapter metricsConfigurerAdapter(final MetricRegistry metricRegistry) {
        return new MetricsConfigurerAdapter() {
            @Override
            public MetricRegistry getMetricRegistry() {
                return metricRegistry;
            }
        };
    }

    @Bean
    @Qualifier("perPathMetricRegistry")
    public MetricRegistry perPathMetricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();

        return metricRegistry;
    }

    @Bean
    @Qualifier("kafkaClientsMetricRegistry")
    public MetricRegistry kafkaClientsMetricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();

        return metricRegistry;
    }

    @Bean
    public MetricRegistry metricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();

        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());

        return metricRegistry;
    }
}
