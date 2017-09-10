package io.spring.cloud.samples.brewery.bottling;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.model.Version;
import io.spring.cloud.samples.brewery.common.model.Wort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static io.spring.cloud.samples.brewery.common.TestConfigurationHolder.TEST_CONFIG;
import static io.spring.cloud.samples.brewery.common.TestConfigurationHolder.TestCommunicationType.FEIGN;
import static io.spring.cloud.samples.brewery.common.TestRequestEntityBuilder.requestEntity;

@Slf4j
class BottlerService {

    private final BottlingWorker bottlingWorker;
    private final PresentingClient presentingClient;
    private final RestTemplate restTemplate;
    private final AsyncRestTemplate asyncRestTemplate;
    private final Tracer tracer;

    public BottlerService(BottlingWorker bottlingWorker, PresentingClient presentingClient,
                          RestTemplate restTemplate, AsyncRestTemplate asyncRestTemplate, Tracer tracer) {
        this.bottlingWorker = bottlingWorker;
        this.presentingClient = presentingClient;
        this.restTemplate = restTemplate;
        this.asyncRestTemplate = asyncRestTemplate;
        this.tracer = tracer;
    }

    /**
     * [OpenTracing::java-spring-cloud] HystrixCommand - Javanica integration
     */
    @HystrixCommand
    void bottle(Wort wort, String processId) {
        log.info("I'm inside bottling");
        Span span = tracer.buildSpan("inside_bottling")
            .startManual();
        try {
            notifyPresenting(processId);
            bottlingWorker.bottleBeer(wort.getWort(), processId, TEST_CONFIG.get());
        } finally {
            span.finish();
        }
    }

    void notifyPresenting(String processId) {
        log.info("I'm inside bottling. Notifying presenting");
        switch (TEST_CONFIG.get().getTestCommunicationType()) {
            case FEIGN:
                callPresentingViaFeign(processId);
                break;
            default:
                useRestTemplateToCallPresenting(processId);
        }
    }

    private void callPresentingViaFeign(String processId) {
        presentingClient.bottlingFeed(processId, FEIGN.name());
    }

	/**
     * [OpenTracing::java-spring-cloud] AsyncRestTemplate with sync @LoadBalanced RestTemplate
     */
    private void useRestTemplateToCallPresenting(String processId) {
        log.info("Notifying presenting about beer. Process id [{}]", processId);
        RequestEntity requestEntity = requestEntity()
                .processId(processId)
                .contentTypeVersion(Version.PRESENTING_V1)
                .serviceName(Collaborators.PRESENTING)
                .url("feed/bottling")
                .httpMethod(HttpMethod.PUT)
                .build();
        URI uri = URI.create("http://presenting/feed/bottling");
        asyncRestTemplate.put(uri, requestEntity);
    }
}
