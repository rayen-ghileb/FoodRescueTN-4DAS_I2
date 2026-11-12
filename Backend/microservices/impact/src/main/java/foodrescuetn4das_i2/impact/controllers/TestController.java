package foodrescuetn4das_i2.impact.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/impact/hello")
    public String helloWorld() {
        return "hello world this is impact microservice";
    }
}