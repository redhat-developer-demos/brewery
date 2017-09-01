package io.spring.cloud.samples.brewery.aggregating;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import io.spring.cloud.samples.brewery.common.MaturingService;
import io.spring.cloud.samples.brewery.common.events.EventGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import io.spring.cloud.samples.brewery.common.TestConfiguration;

import java.util.Collections;

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
	@LoadBalanced
	public RestTemplate loadBalancedRestTemplate(Tracer tracer) {
	    log.info("<<<<< TRACER  AggregationConfiguration >>>>> {} ",tracer);
        RestTemplate restTemplate = new RestTemplate();
        //VERY IMPORTANT
        //FIXME can we use Async Template here ???
        restTemplate.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(tracer)));
        //VERY IMPORTANT
        return restTemplate;
	}

	@Bean
	MaturingServiceUpdater maturingServiceUpdater(IngredientsProperties ingredientsProperties,
												  IngredientWarehouse ingredientWarehouse,
												  MaturingService maturingService,
												  @LoadBalanced RestTemplate restTemplate,
												  EventGateway eventGateway) {
		return new MaturingServiceUpdater(ingredientsProperties,
				ingredientWarehouse, maturingService, restTemplate, eventGateway);
	}

	@Bean
	IngredientsCollector ingredientsCollector(@LoadBalanced RestTemplate restTemplate,
											  IngredientsProxy ingredientsProxy) {
		return new IngredientsCollector(restTemplate, ingredientsProxy);
	}
}

