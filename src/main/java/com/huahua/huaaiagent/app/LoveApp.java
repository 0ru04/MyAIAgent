package com.huahua.huaaiagent.app;

import com.huahua.huaaiagent.advisor.MyLoggerAdvisor;
import com.huahua.huaaiagent.advisor.ReReadingAdvisor;
import com.huahua.huaaiagent.chatmemory.FileBasedChatMemory;
import com.huahua.huaaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.huahua.huaaiagent.rag.QueryRewriter;
import com.huahua.huaaiagent.tools.FileOperationTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;


@Component
@Slf4j
public class LoveApp {

    /**
     * 1.创建 AI 聊天问答功能
     * */
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatClient.Builder chatClientBuilder) {

            // 1. 创建底层的“仓库” (负责存取)
            ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

            // 2. 创建“管理器” (负责包装仓库，管理上下文窗口)
            ChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(chatMemoryRepository)
                    .maxMessages(20) // 设置保留最近的 20 条消息
                    .build();
//        自定义 存储仓库FileBasedChatMemory 初始化基于文件的对话记忆 可解决基于内存对话存储重启丢失问题
//        String fileDir = System.getProperty("user.dir") + "/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

            // 3. 构建 ChatClient，传入包装好的 chatMemory
            this.chatClient = chatClientBuilder
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultAdvisors(
                            //实现对话记忆功能的核心拦截器
                            MessageChatMemoryAdvisor.builder(chatMemory).build(),
                            //调用自定义 advisor
                            new MyLoggerAdvisor()
                            // 自主增强推理 advisor，按需开启(Token翻倍)
                           // new ReReadingAdvisor()
                                    )
                    .build();
    }

    public String doChat(String message,String chatId) {

        ChatResponse response=chatClient
                .prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

        // 从整个响应包里，取出第一条结果，从中拿出消息对象（包含了角色，文本内容以及消息id等元数据），最后提取出里面的纯文本。
        String content=response.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }
    /**
     * 为 LoveApp 添加流式调用方法，通过Stream方法就可以返回 Flux响应式对象
     * */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }


    /**
     * 2. AI 恋爱功能报告 （演示结构化输出）
     * */
    record LoveReport(String title, List<String> suggestions){

    }

    public LoveReport doChatWithReport(String message,String chatId) {

        // 拿着 chatClient，准备一个请求（.prompt），填入用户的话，带上记忆功能（指定 ID 和 数量）,发送出去，最后拿回标准格式的回答。
        LoveReport loveReport=chatClient
                .prompt()
                .system(SYSTEM_PROMPT+"每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表，")
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);

        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    /**
     * 3. AI 知识库问答功能 选用简单易用的 QuestionAnswerAdvisor问答拦截器
     * */

    @Autowired
    private VectorStore loveAppVectorStore;

    @Autowired
    private RetrievalAugmentationAdvisor loveAppRagCloudAdvisor;

    @Autowired
    private VectorStore  pgVectorVectorStore;

    @Autowired
    private QueryRewriter queryRewriter;

    public String doChatWithRag(String message,String chatId){

        // 应用查询重写器
        String rewrittenMessage=queryRewriter.doQueryRewrite(message);

        ChatResponse response=chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())

                //① 应用 RAG 知识库问答
                .advisors( QuestionAnswerAdvisor.builder(loveAppVectorStore).build())

               // ② 应用 RAG 检索增强服务（基于云知识库）
//                .advisors(loveAppRagCloudAdvisor )

               // ③ 应用 RAG 检索增强服务 （基于 PGVector 存储库）
               // .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())

                // ④ 应用 RAG 检索增强服务（基于元数据过滤）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppVectorStore,"单身"
//                        )
//                )
                .call()
                .chatResponse();

        String content=response.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    /**
     * 4. 工具使用
     * */
    @Resource
    private   ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 5.获取自动配置的 MCP 服务提供的所有工具 并提供给 ChatClient
     * */
    @Resource
    private ToolCallbackProvider toolCallbackProvider;


    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }


}