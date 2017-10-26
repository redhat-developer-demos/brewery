package io.spring.cloud.samples.brewery.bottling;

import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import(TestConfiguration.class)
@Slf4j
class BottlingConfiguration {

    @Bean
    BottlerService bottlingService(BottlingWorker bottlingWorker,
                                   PresentingClient presentingClient,
                                   RestTemplate restTemplate,
                                   AsyncRestTemplate asyncRestTemplate,
                                   Tracer tracer) {
        return new BottlerService(bottlingWorker, presentingClient, restTemplate, asyncRestTemplate, tracer);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    AsyncRestTemplate asyncRestTemplate(RestTemplate restTemplate) {
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

