package org.springframework.samples.petclinic.visit.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visit.client.PetValidationClient;
import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.samples.petclinic.visit.dto.UpdateVisitRequestDTO;
import org.springframework.samples.petclinic.visit.dto.VisitMapper;
import org.springframework.samples.petclinic.visit.dto.VisitRequestDTO;
import org.springframework.samples.petclinic.visit.dto.VisitResponseDTO;
import org.springframework.samples.petclinic.visit.repository.VisitRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visits", description = "The Visit Management API")
public class VisitController {

    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;
    private final PetValidationClient petValidationClient;

    @GetMapping
    @Operation(summary = "Get all visits", description = "Returns all visits with pagination support")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visits retrieved successfully")
    })
    public Page<VisitResponseDTO> getAllVisits(Pageable pageable) {
        log.info("Fetching all visits - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return visitRepository.findAll(pageable)
                .map(visitMapper::toResponseDTO);
    }

    @GetMapping("/{visitId}")
    @Operation(summary = "Get a visit by ID", description = "Returns a single visit by its ID or 404 if not found")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visit found"),
        @ApiResponse(responseCode = "404", description = "Visit not found")
    })
    public VisitResponseDTO getVisitById(@PathVariable Integer visitId) {
        log.info("Fetching visit with ID: {}", visitId);
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        return visitMapper.toResponseDTO(visit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new visit", description = "Validates the pet existence before saving the visit")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Visit created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pet or visit data"),
        @ApiResponse(responseCode = "503", description = "Validation service unavailable")
    })
    public VisitResponseDTO createVisit(@Valid @RequestBody VisitRequestDTO request) {
        log.info("Creating visit for pet ID: {}", request.petId());

        // Phase 3 Integration: Synchronous Validation
        petValidationClient.validatePet(request.petId());

        Visit visit = visitMapper.toEntity(request);
        Visit savedVisit = visitRepository.save(visit);

        return visitMapper.toResponseDTO(savedVisit);
    }

    @PutMapping("/{visitId}")
    @Operation(summary = "Update an existing visit", description = "Updates the description and date of an existing visit")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visit updated successfully"),
        @ApiResponse(responseCode = "404", description = "Visit not found")
    })
    public VisitResponseDTO updateVisit(@PathVariable Integer visitId,
                                        @Valid @RequestBody UpdateVisitRequestDTO request) {
        log.info("Updating visit with ID: {}", visitId);
        Visit existingVisit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));

        visitMapper.updateEntity(existingVisit, request);
        Visit updatedVisit = visitRepository.save(existingVisit);

        return visitMapper.toResponseDTO(updatedVisit);
    }

    @DeleteMapping("/{visitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a visit", description = "Removes a visit by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Visit deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Visit not found")
    })
    public void deleteVisit(@PathVariable Integer visitId) {
        log.info("Deleting visit with ID: {}", visitId);
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        visitRepository.delete(visit);
    }

    @GetMapping("/pets/{petId}")
    @Operation(summary = "Get visits by pet ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Visits retrieved successfully")
    })
    public List<VisitResponseDTO> getVisitsByPetId(@PathVariable Integer petId) {
        log.info("Fetching visits for pet ID: {}", petId);
        return visitRepository.findByPetId(petId).stream()
                .map(visitMapper::toResponseDTO)
                .toList();
    }
}
