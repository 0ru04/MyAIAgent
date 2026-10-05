package com.huahua.huaaiagent.tools;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;


class WebSearchToolTest {
    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    void searchWeb() {
        WebSearchTool tool=new WebSearchTool(searchApiKey);
        String query="编程导航问题";
        String result=tool.searchWeb(query);
        assertNotNull(result);

    }
}