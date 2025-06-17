package br.com.phguedes.domain.repositories;

import br.com.phguedes.domain.entities.Checklists;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistsRepository extends JpaRepository<Checklists, Long> {

    boolean existsByBenchAndShiftAndDateTime(String bench, String shift, Instant dateTime);
    long countByDateTimeBetween(Instant start, Instant end);
    Optional<Checklists> findTopByOrderByDateTimeDesc();

    List<Checklists> findByBenchContainingIgnoreCaseAndShiftContainingIgnoreCaseAndDateTimeBetweenAndUser_NameContainingIgnoreCase(
            String bench,
            String shift,
            Instant dateFrom,
            Instant dateTo,
            String userName
    );
}
