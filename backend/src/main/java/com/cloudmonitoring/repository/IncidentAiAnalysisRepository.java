package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.IncidentAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentAiAnalysisRepository extends JpaRepository<IncidentAiAnalysis, Long> {

    List<IncidentAiAnalysis> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);

    Optional<IncidentAiAnalysis> findTopByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}