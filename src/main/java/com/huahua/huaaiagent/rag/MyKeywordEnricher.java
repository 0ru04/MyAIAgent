package com.huahua.huaaiagent.rag;


import com.networknt.schema.Keyword;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自动添加元信息
 * */
@Component
public class MyKeywordEnricher {
    @Resource
    private ChatModel dashscopeChatModel;

    List<Document> enrichDocuments(List<Document> documents){
        KeywordMetadataEnricher enricher=new KeywordMetadataEnricher(this.dashscopeChatModel,5);
        return enricher.apply(documents);
    }
}
