package com.wcs.ai.alibaba.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.wcs.ai.alibaba.hook.LoggingHook;
import com.wcs.ai.alibaba.hook.MessageTrimmingHook;
import com.wcs.ai.alibaba.interceptor.GuardrailInterceptor;
import com.wcs.ai.alibaba.interceptor.ToolErrorInterceptor;
import com.wcs.ai.alibaba.interceptor.ToolMonitoringInterceptor;
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
                                .temperature(0.5D)         // 控制随机性
                                .maxToken(1000) // 最大输出长度
                                .topP(0.9D)                // 核采样参数
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
                .interceptors(new ToolErrorInterceptor(),new ToolMonitoringInterceptor(), new GuardrailInterceptor()) // 拦截器
                .systemPrompt("You are a helpful weather forecast assistant.")
//                .outputType(ResponseFormat.class)
                .outputSchema(Constants.CUSTOM_SCHEMA)
                .saver(new MemorySaver())
                .hooks(new LoggingHook(), new MessageTrimmingHook(), ModelCallLimitHook.builder().runLimit(5).build())  // 钩子，懂的都懂
                .build();
    }

    @Bean
    public ReactAgent architectAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("architect_agent")
                .model(chatModel)
                .instruction(Constants.INSTRUCTION)   // 详细指令
                .build();
    }

    @Bean
    public SkillsAgentHook skillsAgentHook() {
        ClasspathSkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();
        return SkillsAgentHook.builder()
                .skillRegistry(registry)
                .build();
    }

    @Bean
    public ReactAgent skillAgent(ChatModel chatModel, SkillsAgentHook skillsAgentHook) {
        return ReactAgent.builder()
                .name("skills-agent")
                .model(chatModel)
                .hooks(skillsAgentHook)
                .build();
    }


}
