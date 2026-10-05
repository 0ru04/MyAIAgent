package com.huahua.huaaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.huahua.huaaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 手动控制工具执行，自主实现 think和 act 方法
 * */
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    //可用工具类
    private final ToolCallback[] availableTools;
    //保存工具调用信息的响应
    private ChatResponse toolCallChatResponse;
    //工具调用管理者
    private final ToolCallingManager toolCallingManager;
    //禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;
    /** 连续相同工具调用签名上限，超过则判定为死循环 */
    private int maxConsecutiveSameCall = 3;

    /** 连续工具错误上限，超过则熔断 */
    private int maxConsecutiveToolErrors = 3;

    /** 最近工具调用签名，用于检测完全重复循环 */
    private final Deque<String> recentCallSignatures = new ArrayDeque<>();

    /** 连续相同签名计数 */
    private int consecutiveSameSignatureCount = 0;

    /** 连续工具错误计数 */
    private int consecutiveToolErrorCount = 0;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        //保存从huaManus传过来的可用工具
        this.availableTools = availableTools;
        //创建工具执行管理器
        this.toolCallingManager=ToolCallingManager.builder().build();
        //设置框架不自动执行工具，由代码手动控制
        this.chatOptions= DashScopeChatOptions.builder()
                .withToolCallbacks(java.util.Arrays.asList(availableTools))
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前think状态并决定下一步行动
     * 返回是否需要执行行动
     * */
    @Override
    public boolean think() {
        //1.记录用户提示词
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessagesList().add(userMessage);
        }
        //2.拼接用户提示词
        List<Message> messageList = getMessagesList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 3.获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .call()
                    .chatResponse();
            // 4.记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;
            //AssistantMessage的结构 （包括局部变量assistantMessage和成员变量toolCallChatResponse）
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            // 输出提示信息
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            //5.判断是否需要调用工具
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才记录助手消息
                getMessagesList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessagesList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }




    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessagesList(), chatOptions);
        /**
         *ToolCallingManager.executeToolCalls() 会做这些事：
         * 从 toolCallChatResponse 里取出 AssistantMessage；
         * 取出里面的 toolCalls；
         * 根据工具名匹配 ToolCallback；
         * 反序列化参数并执行工具方法；
         * 把每个工具结果封装成 ToolResponse；
         * 生成 ToolResponseMessage；
         * 返回新的 conversationHistory
         * */
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessagesList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);


        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        return results;
    }

//    @Override
//    protected void cleanup() {
//        recentCallSignatures.clear();
//        consecutiveSameSignatureCount = 0;
//        consecutiveToolErrorCount = 0;
//        super.cleanup();
//    }
//
//    /**
//     * 检测工具调用是否陷入死循环
//     */
//    private boolean isLoopDetected(List<AssistantMessage.ToolCall> toolCalls) {
//        if (toolCalls == null || toolCalls.isEmpty()) {
//            return false;
//        }
//
//        String signature = buildCallSignature(toolCalls);
//        recentCallSignatures.addLast(signature);
//
//        if (recentCallSignatures.size() > maxConsecutiveSameCall + 1) {
//            recentCallSignatures.removeFirst();
//        }
//
//        if (recentCallSignatures.size() < 2) {
//            consecutiveSameSignatureCount = 1;
//            return false;
//        }
//
//        String last = recentCallSignatures.peekLast();
//        String previous = null;
//        Iterator<String> iterator = new ArrayDeque<>(recentCallSignatures).descendingIterator();
//        iterator.next();
//        if (iterator.hasNext()) {
//            previous = iterator.next();
//        }
//
//        if (previous != null && Objects.equals(last, previous)) {
//            consecutiveSameSignatureCount++;
//        } else {
//            consecutiveSameSignatureCount = 1;
//        }
//
//        return consecutiveSameSignatureCount >= maxConsecutiveSameCall;
//    }
//
//    /**
//     * 构建工具调用签名：工具名 + 标准化参数
//     */
//    private String buildCallSignature(List<AssistantMessage.ToolCall> toolCalls) {
//        List<String> parts = new ArrayList<>();
//        for (AssistantMessage.ToolCall toolCall : toolCalls) {
//            String args = normalizeArgs(toolCall.arguments());
//            parts.add(toolCall.name() + ":" + args);
//        }
//        Collections.sort(parts);
//        String raw = String.join("|", parts);
//        return sha256(raw);
//    }
//
//    private String normalizeArgs(String arguments) {
//        if (arguments == null) {
//            return "";
//        }
//        return arguments.replaceAll("\\s+", "");
//    }
//
//    private String sha256(String input) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hex = new StringBuilder();
//            for (byte b : hash) {
//                hex.append(String.format("%02x", b));
//            }
//            return hex.toString();
//        } catch (Exception e) {
//            return input;
//        }
//    }
}
