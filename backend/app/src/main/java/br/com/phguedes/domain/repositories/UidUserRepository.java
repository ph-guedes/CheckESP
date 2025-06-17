package br.com.phguedes.domain.repositories;


import br.com.phguedes.domain.entities.UidUser;
import br.com.phguedes.domain.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UidUserRepository extends JpaRepository<UidUser, String> {
    Optional<Users> findByName(String name);
}

