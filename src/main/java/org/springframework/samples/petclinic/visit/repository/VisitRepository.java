package org.springframework.samples.petclinic.visit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.visit.domain.Visit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {
    List<Visit> findByPetId(Integer petId);
}
