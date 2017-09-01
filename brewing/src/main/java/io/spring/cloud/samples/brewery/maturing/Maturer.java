package io.spring.cloud.samples.brewery.maturing;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.MaturingService;
import io.spring.cloud.samples.brewery.common.TestConfigurationHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class Maturer implements MaturingService {

    private final BottlingServiceUpdater bottlingServiceUpdater;

    @Autowired
    public Maturer(BottlingServiceUpdater bottlingServiceUpdater, Tracer tracer) {
        this.bottlingServiceUpdater = bottlingServiceUpdater;
    }

    //FIXME - can the activeSpan be instrumented automatically ??? so we don't need to pass it to BottlingServiceUpdater
    @Override
    public void distributeIngredients(io.spring.cloud.samples.brewery.common.model.Ingredients ingredients,
                                      String processId, String testCommunicationType, SpanContext spanContext) {
        log.info("I'm in the maturing service. Will distribute ingredients");
        TestConfigurationHolder configurationHolder = TestConfigurationHolder.builder().testCommunicationType(TestConfigurationHolder.TestCommunicationType.valueOf(testCommunicationType)).build();
        bottlingServiceUpdater.updateBottlingServiceAboutBrewedBeer(ingredients, processId, configurationHolder, spanContext);
    }
}
