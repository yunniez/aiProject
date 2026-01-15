package org.example.ai;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/resume")
public class ResumeController {

    @GetMapping("/upload")
    public String uploadPage() {
        return "resume/upload"; // 업로드 화면으로 이동
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("file") MultipartFile file, Model model) {
        return "resume/result";
    }
}