package io.spring.cloud.samples.brewery.presenting

import io.opentracing.Tracer
import io.opentracing.contrib.spring.web.client.TracingRestTemplateInterceptor
import io.spring.cloud.samples.brewery.common.BreweryConfiguration
import io.spring.cloud.samples.brewery.common.TestConfiguration
import io.spring.cloud.samples.brewery.common.events.EventSource
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@EnableDiscoveryClient
@EnableFeignClients
@Import([TestConfiguration.class,BreweryConfiguration.class])
@EnableBinding(EventSource.class)
class Application {

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(Tracer tracer) {
        RestTemplate restTemplate = new RestTemplate()
        //VERY IMPORTANT
        //FIXME can we use Async Template here ???
        restTemplate.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(tracer)))
        //VERY IMPORTANT
        return restTemplate
    }

    static void main(String[] args) {
        new SpringApplication(Application.class).run(args)
    }
}
