package com.huahua.huaaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.huahua.huaaiagent.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.util.regex.Pattern;

public class PDFGenerationTool {
    private static final Pattern ILLEGAL_PATH_CHARS = Pattern.compile("[<>:\"/\\\\|?*]");

    /**
     * 清理文件名中的非法字符，避免Windows路径异常
     */
    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        // 把非法字符替换为下划线
        String cleaned = ILLEGAL_PATH_CHARS.matcher(name).replaceAll("_");
        // 确保有.pdf后缀
        if (!cleaned.toLowerCase().endsWith(".pdf")) {
            cleaned = cleaned + ".pdf";
        }
        return cleaned;
    }

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        // 1. 清理文件名
        String safeFileName = sanitizeFileName(fileName);
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + safeFileName;

        try {
            // 1.准备目录
            FileUtil.mkdir(fileDir);

            // 2.核心资源初始化
            // PdfWriter 管道层 建立通往硬盘文件的写入通道
            // PdfDocument 引擎层 接管Writer，负责处理 PDF 的底层格式规范
            // Document 操作层 负责将文字，图片等内容添加到页面上
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // 3，设置全局中文字体 (STSongStd-Light)
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                // 4.添加段落内容
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }

            return "PDF generated successfully to: " + filePath;

        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}