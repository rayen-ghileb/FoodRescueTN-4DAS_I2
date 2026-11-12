package esprit.tn.donationofferservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
    @GetMapping("/malek")
    public String malek() {
        return "Hello malek";
    }

}