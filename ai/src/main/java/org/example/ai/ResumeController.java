package org.example.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public String analyze(@RequestParam("file") MultipartFile file,
                          @RequestParam(value = "jobTitle", defaultValue = "일반 직무") String jobTitle,
                          Model model) throws IOException {

        String content = resumeService.extractTextFromPdf(file);

        String analyzePrompt = String.format("""
            지원 공고(직무): %s
            이력서 내용: %s
            
            당신은 20년 경력의 까다로운 대기업 시니어 채용 팀장입니다. 
            이 이력서를 아주 '비판적'이고 '냉정'하게 평가하세요. 
            
            [평가 가이드라인]
            1. 근거 없는 자신감이나 추상적인 표현은 과감히 감점하세요.
            2. 직무와 관련 없는 경력은 냉정하게 지적하세요.
            3. 수치(숫자)로 증명되지 않은 성과는 신뢰하지 마세요.
            4. 장점보다는 '부족한 점'과 '보완해야 할 점' 위주로 상세히 기술하세요.
                
            [반드시 포함해야 할 필수 데이터 구조]
            반드시 답변 최하단에 아래와 같이 [JSON_DATA] 태그로 감싸서 출력해. 다른 말 섞지마.
            (점수는 60점이 평균이며, 아주 뛰어난 경우에만 80점 이상을 부여하세요.)
            
            [JSON_DATA]
            {
              "score": 55,
              "categories": ["기술역량", "경험수준", "학력/자격", "언어/소통"],
              "points": [50, 40, 60, 70]
            }
            [/JSON_DATA]
            """, jobTitle, content);

        String result = aiService.analyzeResume(analyzePrompt);

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
        return aiService.analyzeResume(resumeContent);
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") Long id, Model model) {
        ResumeReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 리포트가 없습니다. id=" + id));

        model.addAttribute("reportId", report.getId());
        model.addAttribute("result", report.getAnalysisResult());
        model.addAttribute("fileName", report.getFileName());

        return "resume/result";
    }

    @PostMapping("/chat")
    @ResponseBody // JSON이나 문자열로 바로 응답을 줍니다
    public String chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String reportId = payload.get("reportId");

        ResumeReport report = repository.findById(Long.parseLong(reportId)).orElseThrow();

        String chatPrompt = String.format("""
        당신은 이 이력서를 분석한 채용 컨설턴트입니다. 
        다음은 당신이 분석했던 이력서의 요약본입니다: [%s]
        
        사용자의 질문: "%s"
        
        위 이력서 내용을 바탕으로 친절하고 전문적으로 답변해 주세요. 한국어로 답변하세요.
        """, report.getAnalysisResult(), userMessage);

        return aiService.analyzeResume(chatPrompt); // 기존 aiService 재활용!
    }

    @GetMapping("/report/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long id) throws Exception {
        ResumeReport report = repository.findById(id).orElseThrow();

        ObjectMapper mapper = new ObjectMapper();
        String fullAnalysis = report.getAnalysisResult();
        String rawData = extractJsonTag(fullAnalysis);

        JsonNode root = mapper.readTree(rawData);
        List<String> categories = mapper.convertValue(root.get("categories"), new TypeReference<List<String>>(){});
        List<Integer> points = mapper.convertValue(root.get("points"), new TypeReference<List<Integer>>(){});

        String autoComment = getAutoComment(categories, points);

        String cleanContent = fullAnalysis.replaceAll("\\[JSON_DATA\\][\\s\\S]*?\\[/JSON_DATA\\]", "").trim();

        String htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: 'NanumGothic'; line-height: 1.6; color: #333; }
                    .header { text-align: center; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
                    .content { margin-top: 20px; white-space: pre-wrap; }
                    .prescription-box { margin-top: 30px; padding: 15px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>AI 이력서 분석 결과 리포트</h1>
                    <p>파일명: %s</p>
                </div>
                <div class="content">
                    %s
                </div>
                <div class="prescription-box">
                    %s
                </div>
            </body>
            </html>
            """.formatted(report.getFileName(), cleanContent, autoComment);

        byte[] pdfBytes = resumeService.generatePdfFromHtml(htmlContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AI_Resume_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private String getAutoComment(List<String> categories, List<Integer> points) {
        StringBuilder comment = new StringBuilder("<div style='margin-top: 20px; padding: 15px; background: #f8fafc; border-radius: 10px;'>");
        comment.append("<h3 style='color: #1e40af;'>💡 AI 면접관의 핵심 처방전</h3><ul>");

        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            int score = points.get(i);

            if (score < 50) {
                comment.append(String.format("<li><b>%s (%d점):</b> 이 정도면 직무 유기입니다. 관련 프로젝트나 자격증으로 당장 증명하세요.</li>", cat, score));
            } else if (score < 70) {
                comment.append(String.format("<li><b>%s (%d점):</b> 기본은 되어 있으나 임팩트가 부족합니다. '수치'를 사용해서 성과를 다시 쓰세요.</li>", cat, score));
            } else {
                comment.append(String.format("<li><b>%s (%d점):</b> 훌륭합니다. 이 강점을 면접에서 주도권 잡는 무기로 쓰세요.</li>", cat, score));
            }
        }
        comment.append("</ul></div>");
        return comment.toString();
    }

    private String extractJsonTag(String fullText) {
        try {
            // [JSON_DATA]와 [/JSON_DATA] 사이의 내용을 찾는 정규식
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[JSON_DATA\\]([\\s\\S]*?)\\[/JSON_DATA\\]");
            java.util.regex.Matcher matcher = pattern.matcher(fullText);

            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("JSON 태그 추출 실패: " + e.getMessage());
        }
        // 못 찾으면 빈 JSON 형태라도 반환해서 에러 방지
        return "{\"score\":0, \"categories\":[], \"points\":[]}";
    }
}