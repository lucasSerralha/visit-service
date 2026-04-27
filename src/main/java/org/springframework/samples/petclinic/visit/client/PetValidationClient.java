package org.springframework.samples.petclinic.visit.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;

@Service
@Slf4j
public class PetValidationClient {

    private final RestClient restClient;

    public PetValidationClient(RestClient.Builder restClientBuilder) {
        // In a real Eureka setup, we would use the service name: http://customer-service
        this.restClient = restClientBuilder.baseUrl("http://customer-service").build();
    }

    @CircuitBreaker(name = "petService", fallbackMethod = "handlePetValidationFallback")
    @TimeLimiter(name = "petService")
    public void validatePet(Integer petId) {
        log.info("Validating pet {} with Customer Service", petId);
        
        restClient.get()
                .uri("/api/internal/pets/{petId}", petId)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND), (request, response) -> {
                    throw new PetValidationException("Pet with ID " + petId + " does not exist.");
                })
                .toBodilessEntity();
    }

    // Fallback method
    // Important: Fallback method signature must match the original method plus an exception parameter
    public void handlePetValidationFallback(Integer petId, Exception ex) {
        log.error("Fallback triggered for petId {}. Reason: {}", petId, ex.getMessage());
        
        if (ex instanceof PetValidationException) {
            throw (PetValidationException) ex;
        }
        
        throw new RemoteServiceException("Customer Service is currently unavailable or too slow. Please try again later.", ex);
    }
}
