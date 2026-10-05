package com.huahua.huaaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.huahua.huaaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
public class FileOperationTool {

    private final String FILE_DIR= FileConstant.FILE_SAVE_DIR+"/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName){
      String filePath=FILE_DIR+"/"+fileName;
      try{
          return FileUtil.readUtf8String(filePath);
      }catch(Exception e){
          return "Error reading file "+e.getMessage();
      }
    }



    @Tool(description = "Write content to a file")
    public String writeFile(
            @ToolParam(description = "Name of the file to write") String fileName,
            @ToolParam(description = "Content to write to the file") String content) {


        String filePath = FILE_DIR + "/" + fileName;

        try {
            FileUtil.mkdir(FILE_DIR);
            File targetFile = new File(filePath);
            FileUtil.writeUtf8String(content, targetFile);

            return "File written successfully to " + filePath;

        } catch (Exception e) {
            // 如果出错，打印具体错误，方便排查
            e.printStackTrace();
            return "Error writing file: " + e.getMessage();
        }
    }
}
