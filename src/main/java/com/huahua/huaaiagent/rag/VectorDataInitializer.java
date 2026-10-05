package com.huahua.huaaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.vector-store.initialize",
        havingValue = "true",
        matchIfMissing = false
)
public class VectorDataInitializer implements CommandLineRunner {

    private final VectorStore pgVectorVectorStore;
    private final LoveAppDocumentLoader loveAppDocumentLoader;

    public VectorDataInitializer(VectorStore pgVectorVectorStore, LoveAppDocumentLoader loveAppDocumentLoader) {
        this.pgVectorVectorStore = pgVectorVectorStore;
        this.loveAppDocumentLoader = loveAppDocumentLoader;
    }

    @Override
    public void run(String... args) {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 手动分批写入，每批最多 10 条，绕过旧版 spring-ai 的底层 Bug
        int batchSize = 10;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            pgVectorVectorStore.add(batch);
        }
    }
}