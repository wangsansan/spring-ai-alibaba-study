package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgentEngine {

    @Resource
    private ReactAgent weatherPunAgent;

    @SneakyThrows
    public void callAgent() {
        String threadId = UUID.randomUUID().toString();

        // threadId 是给定对话的唯一标识符
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "1").build();

        System.out.println("=========================================");
        // 第一次调用
        AssistantMessage response = weatherPunAgent.call("what is the weather outside?", runnableConfig);
        System.out.println(response.getText());

        // 注意我们可以使用相同的 threadId 继续对话
        response = weatherPunAgent.call("thank you!", runnableConfig);
        System.out.println(response.getText());
    }

}
