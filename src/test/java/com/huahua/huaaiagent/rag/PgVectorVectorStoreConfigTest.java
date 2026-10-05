package com.huahua.huaaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;


@SpringBootTest(properties = {"spring.sql.init.mode=never"})
class PgVectorVectorStoreConfigTest {
    @Resource(name="pgVectorVectorStore")
    VectorStore pgVectorVectorStore;
    @Autowired
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void pgVectorVectorStore() {

        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();



        int batchSize = 10;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> subList = documents.subList(i, end);
            pgVectorVectorStore.add(subList);
        }


        List<Document> results = this.pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学编程").topK(3).build());
        Assertions.assertNotNull(results);
    }
}