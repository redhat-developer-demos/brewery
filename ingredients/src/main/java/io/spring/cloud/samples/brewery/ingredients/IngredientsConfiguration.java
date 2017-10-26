package io.spring.cloud.samples.brewery.ingredients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class IngredientsConfiguration {

	@Bean
	public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
	}

	@Bean
	StubbedIngredientsProperties stubbedIngredientsProperties() {
		return new StubbedIngredientsProperties();
	}
}
