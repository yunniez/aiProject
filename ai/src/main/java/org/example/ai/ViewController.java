package org.example.ai;

import lombok.RequiredArgsConstructor;
import org.example.ai.aiEntity.CareerResult;
import org.springframework.stereotype.Controller; // @RestController가 아닙니다!
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller // 중요: HTML 템플릿을 찾으려면 그냥 @Controller를 써야 합니다.
@RequiredArgsConstructor
public class ViewController {

    private final CareerRepository careerRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ai/history")
    public String history(Model model) {
        List<CareerResult> history = careerRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("history", history);
        return "history";
    }

    @GetMapping("/ai/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        // 1. ID로 DB에서 해당 데이터 한 건 찾기
        CareerResult result = careerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다. id=" + id));
        model.addAttribute("result", result);

        return "detail";
    }
}