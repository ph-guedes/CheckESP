package br.com.phguedes.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ITEMS")
public class Items {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "item_index", nullable = false, unique = true)
    private int index;

    @Column(name = "send_key", nullable = false, unique = true)
    private String key;

    @Column(name = "item_name", nullable = false)
    private String item;

    @Column(name = "question", nullable = false)
    private String question;

    @ElementCollection
    @CollectionTable(name = "ITEM_OPTIONS", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "option_value")
    private List<String> options;
}