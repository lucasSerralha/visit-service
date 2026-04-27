package org.springframework.samples.petclinic.visit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visit.client.PetValidationClient;
import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.samples.petclinic.visit.dto.VisitMapper;
import org.springframework.samples.petclinic.visit.repository.VisitRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
@DisplayName("VisitController REST API Tests")
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VisitRepository visitRepository;

    @MockBean
    private VisitMapper visitMapper;

    @MockBean
    private PetValidationClient petValidationClient;

    private Visit sampleVisit;

    @BeforeEach
    void setUp() {
        sampleVisit = Visit.builder()
                .id(1)
                .visitDate(LocalDate.of(2026, 5, 15))
                .description("Annual checkup")
                .petId(10)
                .build();
    }

    // ========================================================================
    // GET /api/visits — Paginated List
    // ========================================================================
    @Nested
    @DisplayName("GET /api/visits")
    class GetAllVisits {

        @Test
        @DisplayName("should return paginated visits with HTTP 200")
        void shouldReturnPaginatedVisits() throws Exception {
            var responseDtoStub = new org.springframework.samples.petclinic.visit.dto.VisitResponseDTO(
                    1, LocalDate.of(2026, 5, 15), "Annual checkup", 10
            );

            var page = new PageImpl<>(List.of(sampleVisit), PageRequest.of(0, 20), 1);
            given(visitRepository.findAll(any(Pageable.class))).willReturn(page);
            given(visitMapper.toResponseDTO(sampleVisit)).willReturn(responseDtoStub);

            mockMvc.perform(get("/api/visits")
                            .param("page", "0")
                            .param("size", "20")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", is(1)))
                    .andExpect(jsonPath("$.content[0].description", is("Annual checkup")))
                    .andExpect(jsonPath("$.content[0].petId", is(10)))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }
    }

    // ========================================================================
    // GET /api/visits/{visitId} — Single Visit
    // ========================================================================
    @Nested
    @DisplayName("GET /api/visits/{visitId}")
    class GetVisitById {

        @Test
        @DisplayName("should return visit with HTTP 200 when found")
        void shouldReturnVisitWhenFound() throws Exception {
            var responseDtoStub = new org.springframework.samples.petclinic.visit.dto.VisitResponseDTO(
                    1, LocalDate.of(2026, 5, 15), "Annual checkup", 10
            );

            given(visitRepository.findById(1)).willReturn(Optional.of(sampleVisit));
            given(visitMapper.toResponseDTO(sampleVisit)).willReturn(responseDtoStub);

            mockMvc.perform(get("/api/visits/{visitId}", 1)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.description", is("Annual checkup")))
                    .andExpect(jsonPath("$.visitDate", is("2026-05-15")))
                    .andExpect(jsonPath("$.petId", is(10)));
        }

        @Test
        @DisplayName("should return HTTP 404 with ProblemDetail when visit not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(visitRepository.findById(999)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/visits/{visitId}", 999)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Visit Not Found")))
                    .andExpect(jsonPath("$.detail", containsString("999")))
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ========================================================================
    // POST /api/visits — Create
    // ========================================================================
    @Nested
    @DisplayName("POST /api/visits")
    class CreateVisit {

        @Test
        @DisplayName("should create visit with HTTP 201")
        void shouldCreateVisit() throws Exception {
            var requestBody = """
                    {
                        "visitDate": "2026-05-15",
                        "description": "Annual checkup",
                        "petId": 10
                    }
                    """;

            var responseDtoStub = new org.springframework.samples.petclinic.visit.dto.VisitResponseDTO(
                    1, LocalDate.of(2026, 5, 15), "Annual checkup", 10
            );

            given(visitMapper.toEntity(any())).willReturn(sampleVisit);
            given(visitRepository.save(any(Visit.class))).willReturn(sampleVisit);
            given(visitMapper.toResponseDTO(sampleVisit)).willReturn(responseDtoStub);
            doNothing().when(petValidationClient).validatePet(10);

            mockMvc.perform(post("/api/visits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.description", is("Annual checkup")));

            verify(petValidationClient).validatePet(10);
        }
    }

    // ========================================================================
    // PUT /api/visits/{visitId} — Update
    // ========================================================================
    @Nested
    @DisplayName("PUT /api/visits/{visitId}")
    class UpdateVisit {

        @Test
        @DisplayName("should update visit with HTTP 200 when found")
        void shouldUpdateVisitWhenFound() throws Exception {
            var requestBody = """
                    {
                        "visitDate": "2026-06-20",
                        "description": "Follow-up consultation"
                    }
                    """;

            Visit updatedVisit = Visit.builder()
                    .id(1)
                    .visitDate(LocalDate.of(2026, 6, 20))
                    .description("Follow-up consultation")
                    .petId(10)
                    .build();

            var responseDtoStub = new org.springframework.samples.petclinic.visit.dto.VisitResponseDTO(
                    1, LocalDate.of(2026, 6, 20), "Follow-up consultation", 10
            );

            given(visitRepository.findById(1)).willReturn(Optional.of(sampleVisit));
            doNothing().when(visitMapper).updateEntity(eq(sampleVisit), any());
            given(visitRepository.save(sampleVisit)).willReturn(updatedVisit);
            given(visitMapper.toResponseDTO(updatedVisit)).willReturn(responseDtoStub);

            mockMvc.perform(put("/api/visits/{visitId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.description", is("Follow-up consultation")))
                    .andExpect(jsonPath("$.visitDate", is("2026-06-20")));
        }

        @Test
        @DisplayName("should return HTTP 404 when updating non-existent visit")
        void shouldReturn404WhenUpdatingNonExistentVisit() throws Exception {
            var requestBody = """
                    {
                        "visitDate": "2026-06-20",
                        "description": "Follow-up consultation"
                    }
                    """;

            given(visitRepository.findById(999)).willReturn(Optional.empty());

            mockMvc.perform(put("/api/visits/{visitId}", 999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Visit Not Found")));
        }
    }

    // ========================================================================
    // DELETE /api/visits/{visitId} — Delete
    // ========================================================================
    @Nested
    @DisplayName("DELETE /api/visits/{visitId}")
    class DeleteVisit {

        @Test
        @DisplayName("should delete visit with HTTP 204 when found")
        void shouldDeleteVisitWhenFound() throws Exception {
            given(visitRepository.findById(1)).willReturn(Optional.of(sampleVisit));
            doNothing().when(visitRepository).delete(sampleVisit);

            mockMvc.perform(delete("/api/visits/{visitId}", 1))
                    .andExpect(status().isNoContent());

            verify(visitRepository).delete(sampleVisit);
        }

        @Test
        @DisplayName("should return HTTP 404 when deleting non-existent visit")
        void shouldReturn404WhenDeletingNonExistentVisit() throws Exception {
            given(visitRepository.findById(999)).willReturn(Optional.empty());

            mockMvc.perform(delete("/api/visits/{visitId}", 999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Visit Not Found")));
        }
    }
}
