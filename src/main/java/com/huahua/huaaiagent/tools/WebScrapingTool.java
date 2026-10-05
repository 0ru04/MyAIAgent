package com.huahua.huaaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebScrapingTool {
    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "Url of the web page to scrape") String url) {
        try{
            Document doc=Jsoup.connect(url).get();
            return doc.html();
        }catch(Exception e){
            return "Error scraping web page"+e.getMessage();

        }
    }
}
