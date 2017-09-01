package io.spring.cloud.samples.brewery.bottling;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import io.spring.cloud.samples.brewery.common.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
@Import(TestConfiguration.class)
@Slf4j
class BottlingConfiguration {

    @Bean
    BottlerService bottlingService(BottlingWorker bottlingWorker,
                                   PresentingClient presentingClient,
                                   @LoadBalanced RestTemplate restTemplate,
                                   AsyncRestTemplate asyncRestTemplate,
                                   Tracer tracer) {
        return new BottlerService(bottlingWorker, presentingClient, restTemplate, asyncRestTemplate, tracer);
    }

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(Tracer tracer) {
        log.info("<<<<< TRACER  BottlingConfiguration >>>>> {} ",tracer);
        RestTemplate restTemplate = new RestTemplate();
        //VERY IMPORTANT
        //FIXME can we use Async Template here ???
        restTemplate.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(tracer)));
        //VERY IMPORTANT
        return restTemplate;
    }


    @Bean
    AsyncRestTemplate asyncRestTemplate(@LoadBalanced RestTemplate restTemplate) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setTaskExecutor(threadPoolTaskScheduler());
        return new AsyncRestTemplate(requestFactory, restTemplate);
    }

    @Bean(destroyMethod = "shutdown")
    ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}

