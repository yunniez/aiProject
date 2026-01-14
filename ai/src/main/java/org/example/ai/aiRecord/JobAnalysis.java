package org.example.ai.aiRecord;

import java.util.List;
/**
 * AI의 분석 결과를 담을 객체입니다.
 * Record는 필드, 생성자, Getter를 자동으로 만들어줘서 아주 편합니다!
 */
public record JobAnalysis(
        String jobTitle,        // 공고명
        List<String> techStack, // 필요 기술 스택
        String salaryEstimate,  // 예상 연봉 범위
        String advice           // 형님을 위한 조언
) {
}
