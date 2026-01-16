package org.example.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.apache.pdfbox.Loader;

@Service
public class ResumeService {

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) return "파일이 없습니다.";

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            return text;
        } catch (IOException e) {
            e.printStackTrace();
            return "PDF 읽기 실패: " + e.getMessage();
        }
    }
}