package com.huahua.huaaiagent.demo.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderDemoTest {
    @Autowired
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {
        List<Query> queries=multiQueryExpanderDemo.expand("什么才是合格的程序员啊啊啊啊啊啊啊啊啊啊啊啊啊啊");
        Assertions.assertNotNull(queries);
    }
}