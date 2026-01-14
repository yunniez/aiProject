package org.example.ai.aiEntity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CareerResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;      // 공고 명칭
    private int matchingScore;    // 매칭 점수

    @Column(length = 2000)
    private String strongPoints;

    @Column(length = 2000)
    private String weakPoints;

    @Column(length = 2000)
    private String advice;

    private LocalDateTime createdAt; // 저장 시간
}