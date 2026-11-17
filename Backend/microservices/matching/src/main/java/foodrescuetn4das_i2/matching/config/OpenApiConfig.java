package foodrescuetn4das_i2.matching.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "FoodRescue TN — Matching & Pickup API",
    version = "1.0-local",
    description = "Standalone service API. External offer, driver and association IDs are simulated until integration. "
        + "The /api/local endpoints exist only with the local profile.",
    contact = @Contact(name = "FoodRescue TN team")))
public class OpenApiConfig {}
