package com.huahua.huaaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class HttpAiInvoke {
    public static void main(String[] args) {
        // 1. 定义请求地址
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 2. 构建请求头 (Headers)
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + TestApiKey.API_KEY);
        headers.put("Content-Type", "application/json");

        // 3. 构建请求体 (Body) - 使用 Hutool 的 JSONObject 链式调用更简洁
        JSONObject requestBody = new JSONObject();

        // 设置 model
        requestBody.set("model", "qwen-plus");

        // 构建 messages 数组
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");

        // 组装 input
        JSONObject input = new JSONObject();
        input.set("messages", new JSONObject[]{systemMessage, userMessage});
        requestBody.set("input", input);

        // 组装 parameters
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 4. 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)       // 添加请求头
                .body(requestBody.toString()) // 设置请求体
                .execute();                // 执行

        // 5. 处理响应
        if (response.isOk()) {
            System.out.println("请求成功，响应内容：");
            System.out.println(response.body());
        } else {
            System.out.println("请求失败，状态码：" + response.getStatus());
            System.out.println("响应内容：" + response.body());
        }
    }
}



