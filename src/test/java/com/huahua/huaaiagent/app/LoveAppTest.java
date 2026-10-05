package com.huahua.huaaiagent.app;

import com.huahua.huaaiagent.demo.test;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;
    // 加上这两行，意思是：测试时假装有一个 ToolCallbackProvider，先占个坑
    @Resource
    private ToolCallbackProvider toolCallbackProvider; // 加上这一行，占个坑位

    @Test
    void testChat() {
        String chatId= UUID.randomUUID().toString();
        String message="你好，我是程序员花花";
        String answer= loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChatWithReport() {
        String chatId= UUID.randomUUID().toString();
        String message= "你好，我是花花，我想让我的另一半更爱我，但是我不知道怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);


    }

    @Test
    void doChatWithRag() {
        String chatId= UUID.randomUUID().toString();
        String message="我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer= loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }



    @Test
    void doChatWithTools() {

        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message="我的伙伴居住在深圳福田区，请帮我找到5公里以内的聚会地点";
        String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}