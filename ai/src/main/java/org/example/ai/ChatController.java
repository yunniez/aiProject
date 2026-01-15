package org.example.ai;

import org.example.ai.aiEntity.CareerResult;
import org.example.ai.aiRecord.CareerMatching;
import org.example.ai.aiRecord.JobAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final CareerRepository careerRepository;

    public ChatController(ChatClient.Builder builder, CareerRepository careerRepository) {
        this.chatClient = builder.build();
        this.careerRepository = careerRepository;
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
                .entity(JobAnalysis.class); // Spring AI가 JSON을 JobAnalysis 객체변환
    }

    @GetMapping("/ai/coach")
    public CareerResult careerCoach(@RequestParam(value = "jobDescription") String jobDescription) {

        CareerMatching matching = chatClient.prompt()
                .system("너는 전문 헤드헌터야.")
                .user(jobDescription)
                .call()
                .entity(CareerMatching.class);

        // 분석 결과를 엔티티로 변환하여 DB 저장
        CareerResult result = CareerResult.builder()
                .jobTitle(matching.jobTitle())
                .matchingScore(matching.matchingScore())
                .strongPoints(matching.strongPoints())
                .weakPoints(matching.weakPoints())
                .advice(matching.advice())
                .createdAt(LocalDateTime.now())
                .build();

        return careerRepository.save(result);
    }
}