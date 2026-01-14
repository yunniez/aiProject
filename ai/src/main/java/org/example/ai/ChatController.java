package org.example.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    // 생성자 주입 (Spring AI의 ChatClient.Builder를 사용)
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build(); // 저장
    }

    @GetMapping("/ai/test")
    public String chat(@RequestParam(value = "message", defaultValue = "공부 시작!") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/ai/expert")
    public String expertChat(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
                .system("너는 10년 차 자바 시니어 개발자야. 모든 답변은 기술적인 관점에서 명확하게 해주고, 마지막에는 항상 '화이팅 하십쇼 형님!'이라고 붙여줘.")
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/ai/analyze")
    public JobAnalysis analyze() {
        String mockDescription = "Java, Spring Boot 경험 5년 이상, 인공지능에 관심 있는 시니어 개발자 채용";

        return chatClient.prompt()
                .user(mockDescription)
                .call()
                .entity(JobAnalysis.class); // Spring AI가 JSON을 JobAnalysis 객체로 변환!
    }
}