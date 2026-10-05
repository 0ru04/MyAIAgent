package com.huahua.huaaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryRewriter {

    @Autowired
    private ChatClient.Builder chatClientBuilder ;

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashscpoeChatModel) {

        this.chatClientBuilder =ChatClient.builder(dashscpoeChatModel);

        //构建重写查询转换器
        queryTransformer= RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();

    }
    public String doQueryRewrite(String prompt){
        Query query=new Query(prompt);
        Query transformedQuery=queryTransformer.transform(query);
        return transformedQuery.text();
    }

}
