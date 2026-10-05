package com.huahua.huaaiagent.controller;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.huahua.huaaiagent.agent.HuaManus;
import com.huahua.huaaiagent.app.LoveApp;
import com.huahua.huaaiagent.common.BaseResponse;
import com.huahua.huaaiagent.common.ResultUtils;
import com.huahua.huaaiagent.exception.ErrorCode;
import com.huahua.huaaiagent.exception.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {
    @Resource
    private LoveApp loveApp;
    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 开发同步接口
     * */
    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(String message, String chatId){
        ThrowUtils.throwIf(StringUtil.isBlank(message), ErrorCode.PARAMS_ERROR,"用户输出消息不能为空");
        ThrowUtils.throwIf(StringUtil.isBlank(chatId), ErrorCode.PARAMS_ERROR,"chatId不能为空");
        return ResultUtils.success(loveApp.doChat(message,chatId));
    }


    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        HuaManus huaManus = new HuaManus(allTools, dashscopeChatModel);
        return huaManus.runStream(message);
    }


    /** 测试：流式接口①
     * 不能封装，错误必须在流内部处理
     * */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        // defer延迟执行
        return Flux.defer(() -> {
            //1.参数校验，不合法就返回错误 Flux
            if (!StringUtils.hasText(message) || !StringUtils.hasText(chatId)) {
                return Flux.just("[ERROR] message 和 chatId 不能为空");
            }
            //2.拿到业务流
            return loveApp.doChatByStream(message, chatId)
                    //兜底错误，不让连接直接崩 返回包含错误信息的备用流
                    .onErrorResume(e -> {
                        log.error("流式对话异常", e);
                        return Flux.just("[ERROR] " + e.getMessage());
                    });
        });
    }


    /**
     * 测试 流式接口②
     * SseEmitter 版本：同样不能封装，异常走 emitter 自己的回调
    */
@GetMapping(value = "/love_app/chat/sse/emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
    // 创建一个超时时间较长的 SseEmitter
    SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
    if (!StringUtils.hasText(message) || !StringUtils.hasText(chatId)) {
        emitter.completeWithError(new IllegalArgumentException("message 和 chatId 不能为空"));
        return emitter;
    }
    // 获取 Flux 数据流并直接订阅
    // 注册生命回调 .subscribe(onNext, onError, onComplete);
    loveApp.doChatByStream(message, chatId)
            .subscribe(
                    // 处理每条消息
                    chunk -> {
                        try {
                            emitter.send(chunk);
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    // 处理错误
                    emitter::completeWithError,
                    // 处理完成
                    emitter::complete
            );
    return emitter;
    }




}

/**
 * ServerSentEvent.<String>builder()
 *         .id("1")
 *         .event("delta")
 *         .data("你好")
 *         .retry(Duration.ofSeconds(3))
 *         .build();
 *
 * 测试 流式接口③
 @GetMapping(value = "/love_app/chat/sse")
 public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(String message, String chatId) {
 return Flux.defer(() -> {
 if (!StringUtils.hasText(message) || !StringUtils.hasText(chatId)) {
 return Flux.just(
 ServerSentEvent.<String>builder()
 .event("error")
 .data("message 和 chatId 不能为空")
 .build()
 );
 }

 return loveApp.doChatByStream(message, chatId)
 .map(chunk -> ServerSentEvent.<String>builder()
 .data(chunk)
 .build()
 .onErrorResume(e -> {
 log.error("流式对话异常", e);
 return Flux.just(
 ServerSentEvent.<String>builder()
 .event("error")
 .data("message 和 chatId 不能为空")
 .build());
 });
 }
 */
