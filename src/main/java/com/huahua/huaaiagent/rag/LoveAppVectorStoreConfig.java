package com.huahua.huaaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * ETL 第三步骤：
 * 实现初始化默认向量数据库 SimpleVectorStore 并保存文档的方法
 *
 * 负责把各种工具、模型组装起来交给 Spring 的类，用 @Configuration
 * */
@Configuration
//@ConditionalOnProperty(
//        name = "app.vector-store.initialize",
//        havingValue = "true"
//        )
public class LoveAppVectorStoreConfig {
    @Autowired
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Autowired
    private MyTokenTextSplitter myTokenTextSplitter;

   @Autowired
   private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeeEmbeddingModel){
        //创建者模式 创建空的向量存储库 ，并且引入可以将文字转化为向量的 EmbeddingModel 模型
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeeEmbeddingModel).build();
        List<Document> documents=loveAppDocumentLoader.loadMarkdowns();
        // 自定义文档切割器
        //List<Document> splitdocuments = myTokenTextSplitter.splitCustomized(documents);
        List<Document> enrichedDocuments=myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;

    }

}
