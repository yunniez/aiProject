package org.example.ai;

import com.itextpdf.text.pdf.BaseFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class ResumeService {

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) return "파일이 없습니다.";

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return "PDF 읽기 실패: " + e.getMessage();
        }
    }

    public byte[] generatePdfFromHtml(String htmlContent) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        ClassPathResource fontResource = new ClassPathResource("fonts/NanumGothic.ttf");
        String fontPath = fontResource.getFile().getAbsolutePath();

        renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(baos);

        return baos.toByteArray();
    }
}