package org.example.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final AiService aiService;
    private final ResumeReportRepository repository; // 주입 추가

    @GetMapping("/upload")
    public String uploadPage() {
        return "resume/upload"; // 업로드 화면으로 이동
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        String content = resumeService.extractTextFromPdf(file);
        String result = aiService.analyzeResume(content);

        ResumeReport report = new ResumeReport();
        report.setFileName(file.getOriginalFilename());
        report.setContent(content);
        report.setAnalysisResult(result);

        ResumeReport saved = repository.save(report);

        model.addAttribute("reportId", saved.getId());
        model.addAttribute("result", result);
        return "resume/result";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<ResumeReport> reports = repository.findAll();
        System.out.println("불러온 목록 개수: " + reports.size()); // 0개라면 저장이 안 된 것!
        model.addAttribute("reports", repository.findAll());
        return "resume/history";
    }

    @PostMapping("/upload-resume")
    public String uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        String resumeContent = resumeService.extractTextFromPdf(file);
        String analysisResult = aiService.analyzeResume(resumeContent);
        return analysisResult;
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") Long id, Model model) {
        ResumeReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 리포트가 없습니다. id=" + id));

        model.addAttribute("reportId", report.getId());
        model.addAttribute("result", report.getAnalysisResult());
        model.addAttribute("fileName", report.getFileName()); // 파일명도 보여주면 좋죠

        return "resume/result";
    }

    @PostMapping("/chat")
    @ResponseBody // JSON이나 문자열로 바로 응답을 줍니다
    public String chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String reportId = payload.get("reportId");

        // 1. DB에서 해당 이력서 분석 내용을 가져옵니다 (AI에게 문맥을 주기 위함)
        ResumeReport report = repository.findById(Long.parseLong(reportId)).orElseThrow();

        // 2. AI에게 보낼 프롬프트 재구성
        String chatPrompt = String.format("""
        당신은 이 이력서를 분석한 채용 컨설턴트입니다. 
        다음은 당신이 분석했던 이력서의 요약본입니다: [%s]
        
        사용자의 질문: "%s"
        
        위 이력서 내용을 바탕으로 친절하고 전문적으로 답변해 주세요. 한국어로 답변하세요.
        """, report.getAnalysisResult(), userMessage);

        // 3. AI 호출 및 답변 반환
        return aiService.analyzeResume(chatPrompt); // 기존 aiService 재활용!
    }
}