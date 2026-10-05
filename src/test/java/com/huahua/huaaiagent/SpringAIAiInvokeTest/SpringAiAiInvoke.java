package com.huahua.huaaiagent.SpringAIAiInvokeTest;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.vector-store.initialize=false")
public class SpringAiAiInvoke {
    @Resource
    private com.huahua.huaaiagent.demo.invoke.SpringAiAiInvoke springAiAiInvoke;

    @Test
    void testAiInvoke(){
       try{
           springAiAiInvoke.run();
       }catch (Exception e){
           e.printStackTrace();
       }
    }
}
