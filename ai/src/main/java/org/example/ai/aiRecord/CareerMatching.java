package org.example.ai.aiRecord;

public record CareerMatching(
        String jobTitle,      // 공고 명칭
        int matchingScore,    // 내 스택과의 매칭 점수 (0-100)
        String strongPoints,  // 나의 강점
        String weakPoints,    // 보완해야 할 점
        String advice         // AI의 커리어 조언
) {}
