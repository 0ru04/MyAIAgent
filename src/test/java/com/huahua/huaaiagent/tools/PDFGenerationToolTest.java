package com.huahua.huaaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String fileName="学习编程.pdf";
        String content="快来学习编程";
        String result=pdfGenerationTool.generatePDF(fileName,content);
        assertNotNull(result);
    }
}