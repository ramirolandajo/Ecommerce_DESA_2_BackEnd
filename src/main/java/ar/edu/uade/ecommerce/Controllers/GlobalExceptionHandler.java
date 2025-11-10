package ar.edu.uade.ecommerce.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", "Solicitud inválida");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getReason() != null ? ex.getReason() : "Solicitud inválida");
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(ar.edu.uade.ecommerce.Exceptions.InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(ar.edu.uade.ecommerce.Exceptions.InvalidCredentialsException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body); // 401
    }

    @ExceptionHandler(ar.edu.uade.ecommerce.Exceptions.AccountNotVerifiedException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotVerified(ar.edu.uade.ecommerce.Exceptions.AccountNotVerifiedException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body); // 403
    }

    @ExceptionHandler(ar.edu.uade.ecommerce.Exceptions.UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(ar.edu.uade.ecommerce.Exceptions.UserNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    // Otros conflictos de negocio (409)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage() != null ? ex.getMessage() : "Conflicto de negocio");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        // Log completo en el servidor, pero devolver mensaje sanitizado al cliente
        logger.error("Unhandled exception", ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "Ocurrió un error en el servidor. Contacte al administrador.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
