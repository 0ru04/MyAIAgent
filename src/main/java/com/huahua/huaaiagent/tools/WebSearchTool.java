package com.huahua.huaaiagent.tools;

import cn.hutool.http.HttpUtil;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {
    private static final String SEARCH_API_URL="https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey){
        this.apiKey=apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword")String query){
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("q",query);
        paramMap.put("api_key",apiKey);
        paramMap.put("engine","baidu");
        try{
            //1.发送 HTTP Get 请求获取数据
            String response= HttpUtil.get(SEARCH_API_URL,paramMap);
            //2.将响应字符串解析为 Json 对象
            JSONObject jsonObject= JSONUtil.parseObj(response);
            //3.提取指定的 json 数组
            JSONArray organicResults=jsonObject.getJSONArray("organic_results");
            // 4.截取前五个元素
            List<Object> objects=organicResults.subList(0,5);

            String result=objects.stream().map(obj->{
                JSONObject tmpJSONObject=(JSONObject)obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        }catch (Exception e){
           return "Error searching Baidu:" + e.getMessage();
        }
    }
}
