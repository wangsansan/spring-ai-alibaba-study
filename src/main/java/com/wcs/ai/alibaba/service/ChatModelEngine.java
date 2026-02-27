package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wcs.ai.alibaba.tool.DateTimeTools;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatModelEngine {

    @Resource
    private ChatModel chatModel;

    @SneakyThrows
    public void callTest() {
        // 创建带有特定选项的 Prompt；chatModel上也可以配置default options
        DashScopeChatOptions runtimeOptions = DashScopeChatOptions.builder()
                .temperature(0.3)  // 更低的温度，更确定的输出
                .maxToken(500)
                .build();
        ChatResponse chatResponse = chatModel.call(new Prompt(new UserMessage("解释什么是微服务架构"), runtimeOptions));
        System.out.println(chatResponse.getResult().getOutput().getText());
    }

    @SneakyThrows
    public void streamAnswer() {
        // 使用流式 API
        Flux<ChatResponse> responseStream = chatModel.stream(
                new Prompt("详细解释Spring Boot的自动配置原理")
        );

        // 订阅并处理流式响应
        responseStream.subscribe(
                res -> {
                    String content = res.getResult()
                            .getOutput()
                            .getText();
                    System.out.print(content);
                },
                error -> System.err.println("错误: " + error.getMessage()),
                () -> System.out.println("流式响应完成")
        );
    }

    @SneakyThrows
    public void callMulti() {
        List<Message> messages = List.of(
                new SystemMessage("你是一个Java专家"),
                new UserMessage("什么是Spring Boot?"),
                new AssistantMessage("Spring Boot是..."),
                new UserMessage("它有什么优势?")
        );

        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatModel.call(prompt);
        System.out.println(response.getResult().getOutput().getText());
    }

    public void callWithTool() {
        // 定义函数工具
        ToolCallback weatherFunction = FunctionToolCallback.builder("getWeather", (String city) -> {
                    // 实际的天气查询逻辑
                    return "晴朗，25°C";
                })
                .description("获取指定城市的天气")
                .inputType(String.class)
                .build();

        // 使用函数
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .toolCallbacks(List.of(weatherFunction))
                .build();

        Prompt prompt = new Prompt("北京的天气怎么样?", options);
        ChatResponse response = chatModel.call(prompt);
        System.out.println(response.getResult().getOutput().getText());
    }

    /**
     * 使用 ChatClient 进行大模型交互，对chatModel封装了一下，类似于ReactAgent
     * ChatClient是spring-ai提供的，ReactAgent是spring-ai-alibaba提供的
     * - ReactAgent自动记录交互历史记录
     */
    public void callTime() {
        String response = ChatClient.create(chatModel)
                .prompt("Can you set an alarm 10 minutes from now?")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(response);

    }

}
