package br.com.phguedes.domain.repositories;


import br.com.phguedes.domain.entities.Items;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemsRepository extends JpaRepository<Items, Long> {
    Optional<Items> findByItem(String item);
    Optional<Items> findByKey(String key);
    List<Items> findByItemContainingIgnoreCaseAndKeyContainingIgnoreCase(
            String item, String key
    );
    List<Items> findByItemContainingIgnoreCaseAndKeyContainingIgnoreCaseAndIndex(
            String item, String key, int index
    );
    void deleteByKey(String key);
    void deleteByItem(String item);
}