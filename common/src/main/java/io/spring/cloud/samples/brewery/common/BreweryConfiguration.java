package io.spring.cloud.samples.brewery.common;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import brave.sampler.Sampler;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.okhttp3.OkHttpSender;

@Configuration
@AutoConfigureBefore({TracerAutoConfiguration.class, ServerTracingAutoConfiguration.class})
@Slf4j
public class BreweryConfiguration {


    @Bean
    @ConditionalOnProperty(name = "app.tracer", havingValue = "jaeger")
    public Tracer jaegerTracer() {

        log.info("Creating Jaeger OpenTracer");

        Tracer tracer = new com.uber.jaeger.Tracer.Builder("GreenCloud-Brewery",
            new RemoteReporter(new HttpSender("http://jaeger-collector:14268/api/traces", 65000),
                1, 100,
                new Metrics(new StatsFactoryImpl(new NullStatsReporter()))), new ConstSampler(true))
            .build();

        //Register Tracer
        GlobalTracer.register(tracer);

        return GlobalTracer.get();
    }

    @Bean
    @ConditionalOnProperty(name = "app.tracer", havingValue = "zipkin")
    public Tracer zipkinTacer() {

        log.info("Creating Zipkin OpenTracer");

        OkHttpSender okHttpSender = OkHttpSender.builder()
            .encoding(Encoding.JSON)
            .endpoint("http://zipkin:9411/api/v1/spans")
            .build();
        AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();

        Tracing braveTracer = Tracing.newBuilder()
            .localServiceName("spring-boot")
            .reporter(reporter)
            .traceId128Bit(true)
            .sampler(Sampler.ALWAYS_SAMPLE)
            .build();


        Tracer tracer = BraveTracer.create(braveTracer);

        //Register Tracer
        GlobalTracer.register(tracer);

        return GlobalTracer.get();
    }
}
