package gov.hhs.onc.leap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@ComponentScan(basePackages = "gov.hhs.onc")
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        System.out.println("SLS URL: "+ System.getenv("SLS-HOST-URL"));
        System.out.println("SLS URL UNDERSCORE: "+ System.getenv("SLS_HOST_URL"));
        System.out.println("HAPI FHIR URL: "+ System.getenv("HAPI-FHIR-URL"));
        SpringApplication.run(gov.hhs.onc.leap.Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }
}
