package inf.akligo.auth.Payements.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur lors du traitement de la requête");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("cause", ex.getCause() != null ? ex.getCause().getMessage() : "Aucune cause");
        
        System.err.println("🔴 Exception capturée: " + ex.getMessage());
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur inattendue");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("type", ex.getClass().getSimpleName());
        
        System.err.println("🔴 Exception générale: " + ex.getMessage());
        ex.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
