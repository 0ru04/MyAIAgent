package com.huahua.huaaiagent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct模式实现思考和行动的循环模式
 * */

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();
    public abstract String act();
    /**
     * 执行单个步骤的思考和行动，并返回执行结果
     * */
    @Override
    public String step() {
        try {
            boolean shouldAct=think();
            if(!shouldAct){
                return "思考完成，无需执行";
            }
            return act();
        } catch (Exception e) {
            return "单个步骤执行失败:"+e.getMessage();
        }
    }
}
