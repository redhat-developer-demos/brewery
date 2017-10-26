package io.spring.cloud.samples.brewery.presenting.present;

import io.spring.cloud.samples.brewery.common.TestConfigurationHolder;
import io.spring.cloud.samples.brewery.presenting.config.Collaborators;
import io.spring.cloud.samples.brewery.presenting.config.Versions;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//Since we dont use feign with ribbon, its required to spcify the name and url to the client
@FeignClient(name=Collaborators.BREWING,url=Collaborators.BREWING_URL)
@RequestMapping(value = "/ingredients",
		consumes = Versions.BREWING_CONTENT_TYPE_V1,
		produces = MediaType.APPLICATION_JSON_VALUE)
public interface BrewingServiceClient {
	@RequestMapping(method = RequestMethod.POST)
	String getIngredients(String body,
						  @RequestHeader("PROCESS-ID") String processId,
						  @RequestHeader(TestConfigurationHolder.TEST_COMMUNICATION_TYPE_HEADER_NAME) String testCommunicationType);
}
