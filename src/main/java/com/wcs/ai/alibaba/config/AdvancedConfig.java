package com.wcs.ai.alibaba.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.wcs.ai.alibaba.hook.CombinedMemoryHook;
import com.wcs.ai.alibaba.hook.PreferenceLearningHook;
import com.wcs.ai.alibaba.hook.StoreHook;
import com.wcs.ai.alibaba.tool.GetUserInfoTool;
import com.wcs.ai.alibaba.tool.SaveUserInfoTool;
import com.wcs.ai.alibaba.tool.dto.GetMemoryRequest;
import com.wcs.ai.alibaba.tool.dto.SaveMemoryRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvancedConfig {

    @Bean
    public ToolCallback getUserInfoTool() {
        return FunctionToolCallback.builder("getUserInfo", new GetUserInfoTool())
                .description("从长期记忆里查询用户信息")
                .inputType(GetMemoryRequest.class)
                .build();
    }

    @Bean
    public ReactAgent memoryAgent(ChatModel chatModel, ToolCallback getUserInfoTool) {
        return ReactAgent.builder()
                .name("memory_agent")
                .model(chatModel)
                .tools(getUserInfoTool)
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ToolCallback saveUserInfoTool() {
        return FunctionToolCallback.builder("saveUserInfo", new SaveUserInfoTool())
                .description("保存用户信息到长期记忆")
                .inputType(SaveMemoryRequest.class)
                .build();
    }

    @Bean
    public ReactAgent saveMemoryAgent(ChatModel chatModel, ToolCallback saveUserInfoTool) {
        return ReactAgent.builder()
                .name("save_memory_agent")
                .model(chatModel)
                .tools(saveUserInfoTool)
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ReactAgent modelMemoryAgent(ChatModel chatModel) {
        // 创建带有记忆拦截器的Agent
        return ReactAgent.builder()
                .name("memory_agent")
                .model(chatModel)
                .hooks(new StoreHook())
                .saver(new MemorySaver())
                .build();
    }

    @Bean
    public ReactAgent combinedMemoryAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("combined_memory_agent")
                .model(chatModel)
                .hooks(new CombinedMemoryHook())
                .saver(new MemorySaver()) // 短期记忆
                .build();
    }

    @Bean
    public ReactAgent sessionAgent(ChatModel chatModel, ToolCallback getUserInfoTool, ToolCallback saveUserInfoTool) {
        return ReactAgent.builder()
                .name("sessionAgent")
                .model(chatModel)
                .tools(getUserInfoTool, saveUserInfoTool)
                .saver(new MemorySaver()) // 短期记忆
                .build();
    }

    @Bean
    public ReactAgent learningAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("learning_agent")
                .model(chatModel)
                .hooks(new PreferenceLearningHook())
                .saver(new MemorySaver())
                .build();
    }



}
