package org.springframework.samples.petclinic.visit.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "visits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "visit_date")
    @NotNull
    private LocalDate visitDate;

    @Column(name = "description")
    @NotEmpty
    private String description;

    @Column(name = "pet_id")
    @NotNull
    private Integer petId;
}
