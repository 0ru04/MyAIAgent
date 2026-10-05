package com.huahua.huaaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;


/**
 *
 * ETL 过程前两步骤：
 * 读取文档，得到文档列表
 * 转换文档，得到处理后的文档列表
 *
 * 典型的业务组件 加 @Component
 * */

@Component
@Slf4j
public class LoveAppDocumentLoader {

    // 文件扫描器，能一次性把符合规则的所有文件都找出来
    private final ResourcePatternResolver resourcePatternResolver;
    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try{
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                /**
                 * 文档的读取和解析
                 * */
                // 为每篇文档添加特定标签
                String status=fileName.substring(fileName.length()-6,fileName.length()-4);
                MarkdownDocumentReaderConfig config=MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true) //遇到分割线---就切分成新文档
                        .withIncludeCodeBlock(false)  // 忽略代码块
                        .withIncludeBlockquote(false) // 忽略引用块
                        .withAdditionalMetadata("filename",fileName)// 给文档打个标签，记录文件名
                        .withAdditionalMetadata("status",status)  // 元数据标注
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource,config);
                allDocuments.addAll(markdownDocumentReader.get());

            }
        }catch (Exception e){
            log.error("Markdown load error!",e);
        }
        return allDocuments;

    }
}
