package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "score_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ScoreLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false, length = 20)
    private ScoreType scoreType;

    @Column(nullable = false)
    private Integer score;

    public static ScoreLog of(Long userId, ScoreType scoreType, int score) {
        return ScoreLog.builder()
                .userId(userId)
                .scoreType(scoreType)
                .score(score)
                .build();
    }

}
