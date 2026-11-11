package foodrescuetn4das_i2.volunteer.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/volunteer/hello")
    public String helloWorld() {
        return "hello world this is volunteer microservice";
    }
}
