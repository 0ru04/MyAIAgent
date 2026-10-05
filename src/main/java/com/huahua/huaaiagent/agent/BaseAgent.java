package com.huahua.huaaiagent.agent;

import com.huahua.huaaiagent.agent.model.AgentState;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体抽象基类，定义基本信息和多步骤执行流程
 *
 * 提供状态转换、对话上下文存储管理、基于步骤的执行循环的基础功能
 * */
@Data
@Slf4j
public abstract class BaseAgent {
    //1.定义基本信息
    // 核心属性
    private String name;
    //提示
    private String systemPrompt;
    private String nextStepPrompt;
    //状态
    private AgentState state=AgentState.IDLE;
    //执行控制
    private int maxSteps=10;
    private int currentStep=0;
    //LLM
    private ChatClient chatClient;

    //Memory自主维护会话上下文
    private List<Message> messagesList=new ArrayList<>();

    /** 单任务最大执行时长，默认 5 分钟 */
    private Duration maxExecutionTime = Duration.ofMinutes(5);

   /**
    * 运行代理 提供同步输出
    * @param userPrompt 用户提示词
    * @return 执行结果
    * */
   public String run(String userPrompt){
       //判断异常情况
       if(state!=AgentState.IDLE){
           throw new RuntimeException("Connot run agent from state:"+state);
       }
       if(StringUtil.isBlank(userPrompt)){
           throw new RuntimeException("user prompt is empty");
       }
       //更改运行状态
       state=AgentState.RUNNING;
       //记录消息上下文
       messagesList.add(new UserMessage(userPrompt));
       // 保存结果列表
       List<String> results=new ArrayList<>();
       try {
           for(int i=0;i<maxSteps&&state!=AgentState.FINISHED;i++){

               int stepNumber=i+1;
               currentStep=stepNumber;
               //单步执行
               String stepResult=step();
               String result="Step "+stepNumber+": "+stepResult;
               results.add(result);
           }
           if(currentStep>maxSteps){
               state=AgentState.FINISHED;
               results.add("Terminated:Reached max steps("+maxSteps+")");
           }
           return String.join("\n",results);
       } catch (Exception e) {
           state=AgentState.ERROR;
           results.add("Error to Agent"+e.getMessage());
           return "Agent执行错误"+e.getMessage();
       } finally {
           this.cleanup();
       }
   }
    /**
     * 运行代理 提供流式输出
     * @param userPrompt 用户提示词
     * @return SseEmitter实例
     * */
    public SseEmitter runStream(String userPrompt){
        //创建SseEmitter，设置较长的超时时间
        SseEmitter emitter=new SseEmitter(300000L); //5分钟超时

        CompletableFuture.runAsync(()-> {
                    try {
                        //判断异常情况
                        if (state != AgentState.IDLE) {
                            emitter.send("错误：无法从状态运行代理：" + this.state);
                            emitter.complete();
                            return;
                        }
                        if (StringUtil.isBlank(userPrompt)) {
                            emitter.send("错误：不能使用空提示词运行代理：");
                            emitter.complete();
                            return;

                        }
                        //更改运行状态
                        state = AgentState.RUNNING;
                        //记录消息上下文
                        messagesList.add(new UserMessage(userPrompt));
                        // 保存结果列表
                        List<String> results = new ArrayList<>();
                        Instant startTime = Instant.now();
                        try {
                            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {

                                int stepNumber = i + 1;
                                currentStep = stepNumber;
                                //单步执行
                                String stepResult = step();
                                String result = "Step " + stepNumber + ": " + stepResult;
                                //发送每一步的结果
                                emitter.send(result);
                            }
                            if (currentStep > maxSteps) {
                                state = AgentState.FINISHED;
                                results.add("Terminated:Reached max steps(" + maxSteps + ")");
                            }
                            //正常完成
                            emitter.complete();
                        } catch (Exception e) {
                            state = AgentState.ERROR;
                            try {
                                emitter.send("执行错误：" + e.getMessage());
                                emitter.complete();
                            } catch (IOException ex) {
                                emitter.completeWithError(ex);
                            }

                        } finally {
                            this.cleanup();
                        }

                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }


        });

        //处理连接超时，超时断开后台线程 关闭 Http 连接
        emitter.onTimeout(()->{
            this.state=AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        //处理正常或异常关闭情况 ①确保重置状态 ②兜底清理内存
        emitter.onCompletion(()->{
            if(state!=AgentState.RUNNING){
                state=AgentState.FINISHED;
            }
            this.cleanup();
            log.warn("SSE connection completed");
        });

        //推送过程异常 兜底 error 处理
        emitter.onError(ex -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.error("SSE connection error", ex);
        });
        return emitter;
    }

   /**
    * 执行单个步骤
    * */
   public abstract String step();

   /**
    * 清理资源
    * */
   protected void cleanup(){}

}
