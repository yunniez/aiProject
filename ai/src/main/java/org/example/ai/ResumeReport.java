package org.example.ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "resume_report")
public class ResumeReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;        // 올린 파일 이름
    @Lob
    private String content;         // PDF에서 추출한 원문
    @Lob
    private String analysisResult;  // AI가 분석한 리포트 (질문+가이드 포함)

    private LocalDateTime createdAt; // 분석 일시

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}