package com.huahua.huaaiagent;

import com.huahua.huaaiagent.rag.PgVectorVectorStoreConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class HuaAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuaAiAgentApplication.class, args);
    }

}
