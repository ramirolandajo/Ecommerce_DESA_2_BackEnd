package ar.edu.uade.ecommerce.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/ping")
public class PingController {

    @GetMapping
    public String ping() {
        return "pong";
    }
}
