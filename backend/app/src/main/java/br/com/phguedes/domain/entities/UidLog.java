package br.com.phguedes.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "UID_LOG")
public class UidLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(name = "bench", nullable = false)
    private String bench;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "timestamp", nullable = false, unique = true)
    private LocalDateTime timestamp;
}
