package br.com.phguedes.domain.repositories;


import br.com.phguedes.domain.entities.UidLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UidLogRepository extends JpaRepository<UidLog, Long> {
    Optional<UidLog> findFirstByUidAndStatus(String uid, String status);
    void deleteByUid(String uid);
    Optional<UidLog> findByTimestamp(LocalDateTime timestamp);
}

