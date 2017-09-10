package io.spring.cloud.samples.brewery.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin.server.EnableZipkinServer;

@SpringBootApplication
@EnableZipkinServer
public class Application {
    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
