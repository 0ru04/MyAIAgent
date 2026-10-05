package com.huahua.huaaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Vector;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
//@ConditionalOnProperty(
//        name = "app.vector-store.initialize",
//        havingValue = "true"
//)
public class PgVectorVectorStoreConfig {

    @Autowired
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 定义PgVectorStore数据库配置
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(false)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10)
                .build(); // 生成实例

//        // 加载 Markdown 文档并写入向量数据库
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        /**
//         * 此处可自定义文档切割方式，再存储数据库
//         * */
//        vectorStore.add(documents);

        return vectorStore;
    }
}
