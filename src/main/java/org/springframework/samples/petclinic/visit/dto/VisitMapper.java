package org.springframework.samples.petclinic.visit.dto;

import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {

    public Visit toEntity(VisitRequestDTO dto) {
        return Visit.builder()
                .visitDate(dto.visitDate())
                .description(dto.description())
                .petId(dto.petId())
                .build();
    }

    public VisitResponseDTO toResponseDTO(Visit entity) {
        return new VisitResponseDTO(
                entity.getId(),
                entity.getVisitDate(),
                entity.getDescription(),
                entity.getPetId()
        );
    }

    public void updateEntity(Visit entity, UpdateVisitRequestDTO dto) {
        entity.setVisitDate(dto.visitDate());
        entity.setDescription(dto.description());
    }
}
