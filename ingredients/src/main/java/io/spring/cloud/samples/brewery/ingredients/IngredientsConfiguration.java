package io.spring.cloud.samples.brewery.ingredients;

import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
class IngredientsConfiguration {

	@Bean
	@LoadBalanced
	public RestTemplate loadBalancedRestTemplate(Tracer  tracer) {
        RestTemplate restTemplate = new RestTemplate();
        //VERY IMPORTANT
        //FIXME can we use Async Template here ???
        restTemplate.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(tracer)));
        //VERY IMPORTANT
        return restTemplate;
	}

	@Bean
	StubbedIngredientsProperties stubbedIngredientsProperties() {
		return new StubbedIngredientsProperties();
	}
}
