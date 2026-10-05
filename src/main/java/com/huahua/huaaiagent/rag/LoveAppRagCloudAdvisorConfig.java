package com.huahua.huaaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 先编写一个配置类，用于初始化 基于云知识库 的检索增强顾问 RetrievalAugmentationAdvisor 的 Bean loveAppRagCloudAdvisor
 * */

@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Value( "${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;


    /**
     * 初始化并配置一个专门针对 “恋爱大师” 知识库的 AI 问答顾问
     * */
    @Bean
    public RetrievalAugmentationAdvisor loveAppRagCloudAdvisor() {
        // 1.使用 builder() 创建实例，并设置 API Key
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();

        // 2.创建文档检索器
        final String KNOWLEDGE_INDEX = "恋爱大师";
        DocumentRetriever documentRetriever=new DashScopeDocumentRetriever(dashScopeApi,
                //使用构建者模式来设置检索选项
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());

            return RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)
                    .build();
        }

}
