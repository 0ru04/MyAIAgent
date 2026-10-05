package com.huahua.huaaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * DocumentRetriever 是 SpringAI 提供的文档检索器，从向量存储中检索与输入查询语义相似的文档
 *
 * 它支持基于元数据的过滤，设置相似度阈值，设置返回的结果数
 * */
@Slf4j
public class LoveAppRagCustomAdvisorFactory {

   public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status){
        Filter.Expression expression=new FilterExpressionBuilder()
                .eq("status",status)
                .build();
       DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
               .vectorStore(vectorStore)
               .similarityThreshold(0.5)
               .topK(3)
               .filterExpression(expression)
               .build();

       return RetrievalAugmentationAdvisor.builder()
               .documentRetriever(retriever)
               .queryAugmenter(LoveAppContextualQueryAugmentFactory.createInstance())
               .build();
   }
}
