package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sequences")
@Getter
@NoArgsConstructor
public class Sequence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String title;

    public static Sequence create(Long userId, String title) {
        Sequence sequence = new Sequence();
        sequence.userId = userId;
        sequence.title = title;
        return sequence;
    }
}