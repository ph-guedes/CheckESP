package br.com.phguedes.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "CHECKLIST_ITEMS")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "response", nullable = false)
    private String response;

    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklists checklists;
}
