package com.huahua.huaaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.huahua.huaaiagent.constant.FileConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class ResourceDownloadTool {
    @Tool(description = "Download a resource from a given url")
    public String downloadResource(@ToolParam(description = "url of the resource to download") String url,
                                  @ToolParam(description = "name of the file to save the download resource")String fileName) {

    String fileDir= FileConstant.FILE_SAVE_DIR+"/download";
    String filePath= fileDir+"/"+fileName;
       try{
           FileUtil.mkdir(fileDir);
           HttpUtil.downloadFile(url,new File(filePath));
           return "Resource downloaded successfully to: "+filePath;
       }catch(Exception e){
           return "Error downloading resource:"+e.getMessage();
       }

    }
}
