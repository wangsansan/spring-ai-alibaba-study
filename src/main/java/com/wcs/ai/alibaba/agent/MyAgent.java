package com.wcs.ai.alibaba.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;

public class MyAgent {

    @SneakyThrows
    public static void main(String[] args) {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .instruction("You are a helpful weather forecast assistant.")
                .build();

        // 运行 Agent
        AssistantMessage assistantMessage = agent.call("北京昨天的温度");
        System.out.println(assistantMessage.getText());
    }

}
