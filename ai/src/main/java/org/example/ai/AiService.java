package org.example.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String analyzeResume(String rawText) {
        String prompt = String.format("""
            [SYSTEM RULE: ANSWER IN KOREAN ONLY]
            [IMPORTANT RULE: ANSWER IN KOREAN ONLY. NO CHINESE, NO ENGLISH UNLESS TECHNICAL TERMS]
            당신은 한국의 IT 대기업 기술 면접관입니다. 모든 답변은 반드시 한국어(Korean)로 작성하세요.
            
            분석할 이력서 내용은 다음과 같습니다:
            %s
            
            위 이력서를 바탕으로:
            1. 역량 요약
            2. 면접 질문 리스트
            3. 보완 가이드 (학습 방향)
            4. 총평
            
            위 순서대로 '한국어'로 상세히 리포트를 작성하세요. 
            영어로 작성하면 절대로 안 됩니다. 한국어로만 답변하세요.
            """, rawText);

        // 2. AI에게 요청
        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3");
        request.put("prompt", prompt);
        request.put("stream", false);

        Map<String, Object> response = restTemplate.postForObject(OLLAMA_URL, request, Map.class);
        return (String) response.get("response");
    }
}