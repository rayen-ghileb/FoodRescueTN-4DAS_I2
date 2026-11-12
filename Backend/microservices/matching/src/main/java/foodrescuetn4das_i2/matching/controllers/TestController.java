package foodrescuetn4das_i2.matching.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/matching/hello")
    public String hello() {
        return "hello im matching and pickup service";
    }
}
