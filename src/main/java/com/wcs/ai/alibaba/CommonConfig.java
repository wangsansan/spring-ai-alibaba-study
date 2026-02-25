package com.wcs.ai.alibaba;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.wcs.ai.alibaba.common.ResponseFormat;
import com.wcs.ai.alibaba.tool.UserLocationTool;
import com.wcs.ai.alibaba.tool.WeatherForLocationTool;
import com.wcs.ai.alibaba.tool.WeatherTool;
import com.wcs.ai.alibaba.utils.Constants;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    public static final String MODEL_API_KEY = "AI_DASHSCOPE_API_KEY";

    @Bean
    public ChatModel chatModel() {
        // 初始化 ChatModel
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv(MODEL_API_KEY))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .model(DashScopeChatModel.DEFAULT_MODEL_NAME)
                                .temperature(0.5D)
                                .maxToken(1000)
                                .build()
                ).build();
    }

    @Bean
    public ReactAgent weatherAgent(ChatModel chatModel) {
        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();

        // 创建 agent
        return ReactAgent.builder()
                .name("weather_agent")
                .model(chatModel)
                .tools(weatherTool)
                .systemPrompt("You are a helpful weather forecast assistant.")
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ToolCallback weatherTool() {
        return FunctionToolCallback
                .builder("getWeatherForLocation", new WeatherForLocationTool())
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();
    }


    @Bean
    public ToolCallback userLocationTool() {
        return FunctionToolCallback
                .builder("getUserLocation", new UserLocationTool())
                .description("Retrieve user location based on user ID")
                .inputType(String.class)
                .build();
    }

    @Bean
    public ReactAgent weatherPunAgent(ChatModel chatModel, ToolCallback weatherTool, ToolCallback userLocationTool) {
        // 创建 agent
        return ReactAgent.builder()
                .name("weather_pun_agent")
                .model(chatModel)
                .systemPrompt(Constants.SYSTEM_PROMPT)
                .tools(weatherTool, userLocationTool)
                .systemPrompt("You are a helpful weather forecast assistant.")
//                .outputType(ResponseFormat.class)
                .outputSchema(Constants.CUSTOM_SCHEMA)
                .saver(new MemorySaver())
                .build();
    }

}
