package io.spring.cloud.samples.brewery.aggregating;

import io.spring.cloud.samples.brewery.common.MaturingService;
import io.spring.cloud.samples.brewery.common.TestConfiguration;
import io.spring.cloud.samples.brewery.common.events.EventGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import(TestConfiguration.class)
@Slf4j
class AggregationConfiguration {

    @Bean
    IngredientsProperties ingredientsProperties() {
        return new IngredientsProperties();
    }

    @Bean
    AsyncRestTemplate asyncRestTemplate() {
        return new AsyncRestTemplate();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    MaturingServiceUpdater maturingServiceUpdater(IngredientsProperties ingredientsProperties,
                                                  IngredientWarehouse ingredientWarehouse,
                                                  MaturingService maturingService,
                                                  RestTemplate restTemplate,
                                                  EventGateway eventGateway) {
        return new MaturingServiceUpdater(ingredientsProperties,
            ingredientWarehouse, maturingService, restTemplate, eventGateway);
    }

    @Bean
    IngredientsCollector ingredientsCollector(RestTemplate restTemplate,
                                              IngredientsProxy ingredientsProxy) {
        return new IngredientsCollector(restTemplate, ingredientsProxy);
    }
}

