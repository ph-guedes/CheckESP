package br.com.phguedes.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "CHECKLISTS")
public class Checklists {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "bench", nullable = false)
    private String bench;

    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "date", nullable = false)
    private Instant dateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Setter
    @OneToMany(mappedBy = "checklists", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChecklistItem> items;
}
