package com.huahua.huaaiagent.agent;

import com.huahua.huaaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
/**
 * spring启动 HuaManus最终拥有：
 * name = "huaManus"
 * systemPrompt：设定全能助手身份；
 * nextStepPrompt：告诉它根据需求选择工具；
 * maxSteps = 20；
 * chatClient：绑定 DashScope 模型；
 * chatOptions：携带工具列表，且关闭自动工具执行；
 * messagesList：初始为空。
 *
 * */
@Component
public class HuaManus extends ToolCallAgent {

    public HuaManus(ToolCallback[] allTools, ChatModel dashScopeChatModel) {
        super(allTools);
        this.setName("huaManus");
        String SYSTEM_PROMPT = """  
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}

