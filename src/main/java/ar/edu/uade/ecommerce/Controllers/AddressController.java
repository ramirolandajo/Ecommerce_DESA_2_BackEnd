package ar.edu.uade.ecommerce.Controllers;

import ar.edu.uade.ecommerce.Entity.Address;
import ar.edu.uade.ecommerce.Entity.User;
import ar.edu.uade.ecommerce.Service.AddressService;
import ar.edu.uade.ecommerce.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthService authService;

    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestHeader("Authorization") String authHeader, @RequestBody Address address) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        address.setUser(user);
        Address saved = addressService.save(address);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Address> editAddress(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id, @RequestBody Address address) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        Address existing = addressService.findById(id);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        existing.setDescription(address.getDescription());
        Address updated = addressService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader("Authorization") String authHeader, @PathVariable Integer id) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        User user = authService.getUserByEmail(email);
        Address existing = addressService.findById(id);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        addressService.delete(id);
        return ResponseEntity.ok().build();
    }

}
