package com.huahua.huaaiagent.rag;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * RAG 检索阶段的优化技巧
 * 空上下文处理
 *
 * */
public class LoveAppContextualQueryAugmentFactory {
    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate prompt=new PromptTemplate(
                """
     你应该输出下面的内容：
     抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦。
     """
        );
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false) //不允许在“空上下文”（即没有检索到任何相关文档）的情况下让大模型继续回答。
                .emptyContextPromptTemplate(prompt) //当检索不到内容时，要强制大模型按照你指定的 prompt 模板来输出回复。
                .build();
    }
}
