package io.spring.cloud.samples.brewery.maturing;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.BottlingService;
import io.spring.cloud.samples.brewery.common.TestConfigurationHolder;
import io.spring.cloud.samples.brewery.common.events.Event;
import io.spring.cloud.samples.brewery.common.events.EventGateway;
import io.spring.cloud.samples.brewery.common.events.EventType;
import io.spring.cloud.samples.brewery.common.model.Ingredients;
import io.spring.cloud.samples.brewery.common.model.Version;
import io.spring.cloud.samples.brewery.common.model.Wort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import static io.spring.cloud.samples.brewery.common.TestConfigurationHolder.TestCommunicationType.FEIGN;
import static io.spring.cloud.samples.brewery.common.TestRequestEntityBuilder.requestEntity;

@Slf4j
class BottlingServiceUpdater {

    private final BrewProperties brewProperties;
    private final PresentingServiceClient presentingServiceClient;
    private final BottlingService bottlingService;
    private final RestTemplate restTemplate;
    private final EventGateway eventGateway;
    private final Tracer tracer;

    public BottlingServiceUpdater(BrewProperties brewProperties,
                                  Tracer tracer,
                                  PresentingServiceClient presentingServiceClient,
                                  BottlingService bottlingService,
                                  RestTemplate restTemplate, EventGateway eventGateway) {
        this.brewProperties = brewProperties;
        this.presentingServiceClient = presentingServiceClient;
        this.bottlingService = bottlingService;
        this.restTemplate = restTemplate;
        this.tracer = tracer;
        this.eventGateway = eventGateway;
    }

    @Async
    public void updateBottlingServiceAboutBrewedBeer(final Ingredients ingredients, String processId,
                                                     TestConfigurationHolder configurationHolder) {

        log.info("inside_maturing ");
        Span span = tracer.buildSpan("inside_maturing")
            .startManual();

        try {
            TestConfigurationHolder.TEST_CONFIG.set(configurationHolder);
            log.info("Updating bottling service. Current process id is equal [{}]", processId);
            notifyPresentingService(processId);
            brewBeer();
            //FIXME this will not work as it is done via sleuth stream - need instrumentation here
            eventGateway.emitEvent(Event.builder().eventType(EventType.BEER_MATURED).processId(processId).build());
            notifyBottlingService(ingredients, processId);
        } finally {
            span.finish();
        }
    }

    private void brewBeer() {
        try {
            Long timeout = brewProperties.getTimeout();
            log.info("Brewing beer... it will take [{}] ms", timeout);
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("Exception occurred while brewing beer", e);
        }
    }

    private void notifyPresentingService(String correlationId) {
        log.info("Calling presenting from maturing");
        Span scope = this.tracer.
            buildSpan("calling_presenting_from_maturing")
            .startManual();
        switch (TestConfigurationHolder.TEST_CONFIG.get().getTestCommunicationType()) {
            case FEIGN:
                callPresentingViaFeign(correlationId);
                break;
            default:
                useRestTemplateToCallPresenting(correlationId);
        }
        scope.finish();
    }

    private void callPresentingViaFeign(String correlationId) {
        presentingServiceClient.maturingFeed(correlationId, FEIGN.name());
    }

    /**
     * [OpenTracing::java-spring-cloud] HystrixCommand - Javanica integration
     */
    @HystrixCommand
    public void notifyBottlingService(Ingredients ingredients, String correlationId) {
        log.info("Calling bottling from maturing");
        Span scope = this.tracer.buildSpan("calling_bottling_from_maturing")
            .startManual();
        bottlingService.bottle(new Wort(getQuantity(ingredients)), correlationId, FEIGN.name());
        scope.finish();
    }

    private void useRestTemplateToCallPresenting(String processId) {
        log.info("Calling presenting - process id [{}]", processId);
        restTemplate.exchange(requestEntity()
            .processId(processId)
            .contentTypeVersion(Version.PRESENTING_V1)
            .serviceName(Collaborators.PRESENTING)
            .url("feed/maturing")
            .httpMethod(HttpMethod.PUT)
            .build(), String.class);
    }

    private Integer getQuantity(Ingredients ingredients) {
        Assert.notEmpty(ingredients.ingredients);
        return ingredients.ingredients.get(0).getQuantity();
    }

}

