package io.spring.cloud.samples.brewery.common;

import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.senders.HttpSender;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.autoconfig.ServerTracingAutoConfiguration;
import io.opentracing.contrib.spring.web.autoconfig.TracerAutoConfiguration;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@AutoConfigureBefore({TracerAutoConfiguration.class, ServerTracingAutoConfiguration.class})
@Slf4j
public class BreweryConfiguration {

    @Bean
    @Primary
    public Tracer jaegerTracer() {

        log.info("Creating Jaeger OpenTracer");

        return new com.uber.jaeger.Tracer.Builder("GreenCloud-Brewery",
            new RemoteReporter(new HttpSender("http://jaeger-collector:14268/api/traces", 65000),
                1, 100,
                new Metrics(new StatsFactoryImpl(new NullStatsReporter()))), new ConstSampler(true))
            .build();
    }
}
