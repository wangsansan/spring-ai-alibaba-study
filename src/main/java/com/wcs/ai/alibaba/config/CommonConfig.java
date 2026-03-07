package com.wcs.ai.alibaba.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.wcs.ai.alibaba.hook.LoggingHook;
import com.wcs.ai.alibaba.hook.MessageTrimmingHook;
import com.wcs.ai.alibaba.interceptor.GuardrailInterceptor;
import com.wcs.ai.alibaba.interceptor.ToolErrorInterceptor;
import com.wcs.ai.alibaba.interceptor.ToolMonitoringInterceptor;
import com.wcs.ai.alibaba.tool.PoetTool;
import com.wcs.ai.alibaba.tool.UserLocationTool;
import com.wcs.ai.alibaba.tool.WeatherForLocationTool;
import com.wcs.ai.alibaba.tool.WeatherTool;
import com.wcs.ai.alibaba.utils.Constants;
import com.wcs.ai.alibaba.workflow.node.PreprocessorNode;
import com.wcs.ai.alibaba.workflow.node.ValidatorNode;
import lombok.SneakyThrows;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CommonConfig {

    public static final String MODEL_API_KEY = "AI_DASHSCOPE_API_KEY";

    @Bean
    public DashScopeApi dashScopeApi() {
        return  DashScopeApi.builder()
                .apiKey(System.getenv(MODEL_API_KEY))
                .build();
    }

    /**
     * spring-ai-alibaba会自动注入一个chatModel，可以不实例化
     * @param dashScopeApi
     * @return
     */
//    @Bean
    public ChatModel chatModel(DashScopeApi dashScopeApi) {
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

    @Bean
    public ReactAgent approvalAgent(ChatModel chatModel) {
        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather_v1", new WeatherTool())
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();
        // 配置检查点保存器（人工介入需要检查点来处理中断）
        MemorySaver memorySaver = new MemorySaver();

        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("get_weather_v1", ToolConfig.builder()
                        .description("使用查询天气工具需要审批")
                        .build())
                .build();
        // 创建Agent
        return ReactAgent.builder()
                .name("approval_agent")
                .model(chatModel)
                .tools(weatherTool)
                .hooks(List.of(humanInTheLoopHook))
                .saver(memorySaver)
                .build();
    }

    @Bean
    public ToolCallback poetTool() {
        return FunctionToolCallback
                .builder("poemTool", new PoetTool())
                .description("create a poem about some subject")
                .inputType(String.class)
                .build();
    }

    @Bean
    public ReactAgent poetAgent(ChatModel chatModel, ToolCallback poetTool) {
        // 1. 配置检查点
        MemorySaver memorySaver = new MemorySaver();

        // 2. 创建人工介入Hook
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("poemTool", ToolConfig.builder()
                        .description("请确认诗歌创作操作")
                        .build())
                .build();

        // 3. 创建Agent
        return ReactAgent.builder()
                .name("poet_agent")
                .model(chatModel)
                .tools(List.of(poetTool))
                .hooks(List.of(humanInTheLoopHook))
                .saver(memorySaver)
                .build();
    }

    @Bean
    public MemorySaver saver() {
        return new MemorySaver();
    }


    @Bean
    public ReactAgent qaAgent(ChatModel chatModel, MemorySaver saver) {
        // 1. 创建工具回调
        ToolCallback searchTool = FunctionToolCallback
                .builder("search", (args) -> "搜索结果：AI Agent是能够感知环境、自主决策并采取行动的智能系统。")
                .description("搜索工具，用于查找相关信息")
                .inputType(String.class)
                .build();

        // 3. 创建带有人工介入Hook的ReactAgent
        return ReactAgent.builder()
                .name("qa_agent")
                .model(chatModel)
                // 注意此处的prompt，要限定agent只能使用search工具，否则可能通过其他方式查询结果，导致工作流不会中断
                .instruction("你是一个问答专家，负责回答用户的问题。如果需要搜索信息，必须且只能使用search工具。 用户问题：{cleaned_input}")
                .outputKey("qa_result")
                .saver(saver)
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("search", ToolConfig.builder()
                                .description("搜索操作需要人工审批，请确认是否执行搜索")
                                .build())
                        .build())
                .tools(searchTool)
                .build();
    }

    @Bean
    public KeyStrategyFactory keyStrategyFactory() {
        return () -> {
            Map<String, KeyStrategy> strategyMap = new HashMap<>();
            strategyMap.put("input", new ReplaceStrategy());
            strategyMap.put("cleaned_input", new ReplaceStrategy());
            strategyMap.put("qa_result", new ReplaceStrategy());
            strategyMap.put("is_valid", new ReplaceStrategy());
            return strategyMap;
        };
    }

    // 构建工作流
    @SneakyThrows
    @Bean
    public StateGraph workflow(KeyStrategyFactory keyStrategyFactory, ReactAgent qaAgent) {
        // 7. 构建工作流
        StateGraph workflow = new StateGraph(keyStrategyFactory);

        // 添加普通Node
        workflow.addNode("preprocess", AsyncNodeAction.node_async(new PreprocessorNode()));
        // 添加Agent Node（嵌套的ReactAgent）
        // 此处的第一个入参不能修改，因为asNode方法会传入agent的name属性，workflow会做校验匹配
        workflow.addNode(qaAgent.name(), qaAgent.asNode(
                true,   // includeContents: 传递父图的消息历史
                false   // includeReasoning: 不返回推理过程
        ));
        workflow.addNode("validate", AsyncNodeAction.node_async(new ValidatorNode()));

        // 定义流程：预处理 -> Agent处理 -> 验证
        workflow.addEdge(StateGraph.START, "preprocess");
        workflow.addEdge("preprocess", qaAgent.name());
        workflow.addEdge(qaAgent.name(), "validate");

        // 条件边：验证通过则结束，否则重新处理
        workflow.addConditionalEdges(
                "validate",
                AsyncEdgeAction.edge_async(state -> {
                    Boolean isValid = state.value("is_valid", false);
                    return isValid ? "end" : qaAgent.name();
                }),
                Map.of(
                        "end", StateGraph.END,
                        qaAgent.name(), qaAgent.name()
                )
        );
        return workflow;
    }

    //编译工作流
    @SneakyThrows
    @Bean
    public CompiledGraph compiledGraph(StateGraph workflow, MemorySaver saver) {
        return workflow.compile(
                CompileConfig.builder()
                        .saverConfig(SaverConfig.builder().register(saver).build())
                        .build()
        );
    }


}
