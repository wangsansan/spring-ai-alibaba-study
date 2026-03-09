package com.wcs.ai.alibaba.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// ToolInterceptor - 监控和错误处理
public class ToolMonitoringInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        long startTime = System.currentTimeMillis();
        try {
            ToolCallResponse response = handler.call(request);
            logSuccess(request, System.currentTimeMillis() - startTime);
            return response;
        } catch (Exception e) {
            logError(request, e, System.currentTimeMillis() - startTime);
            return ToolCallResponse.error(request.getToolCallId(), request.getToolName(),
                    "工具执行遇到问题，请稍后重试");
        }
    }

    private void logSuccess(ToolCallRequest request, Long usedTime){
        log.info("success, request:[{}], usedTime:[{}]", JSON.toJSONString(request), usedTime);
    }

    private void logError(ToolCallRequest request, Exception e, Long usedTime) {
        log.error("error, request:[{}], usedTime:[{}]", JSON.toJSONString(request), usedTime, e);
    }

    @Override
    public String getName() {
        return "ToolMonitoringInterceptor";
    }
}
