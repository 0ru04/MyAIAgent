package com.huahua.huaaiagent.controller;

import com.huahua.huaaiagent.common.BaseResponse;
import com.huahua.huaaiagent.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
