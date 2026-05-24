package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sequenceItems")
@Getter
@NoArgsConstructor
public class SequenceItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sequence_id", nullable = false)
    private Long sequenceId;

    @Column(name = "emoji_id", nullable = false)
    private Long emojiId;

    @Column(name = "order_index", nullable = false, columnDefinition = "TINYINT")
    private Integer orderIndex;

    @Column(name = "multiplier", nullable = false, precision = 2, scale = 1) // 전체 자리수: 2자리, 소숫점 이하: 1자리
    private BigDecimal multiplier;

}
