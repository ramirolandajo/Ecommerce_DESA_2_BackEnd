package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

}

