package com.wcs.ai.alibaba.rag.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QueryEnhancementHook extends AgentHook {
    private final ChatModel chatModel;
    private static final String ENHANCED_QUERY_KEY = "enhanced_query";

    public QueryEnhancementHook(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "query_enhancement";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        // 从状态中提取用户查询
        Optional<Object> messagesOpt = state.value("messages");
        if (messagesOpt.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        @SuppressWarnings("unchecked")
        List<Message> messages = (List<Message>) messagesOpt.get();

        // 提取最后一个用户消息作为查询
        String userQuery = messages.stream()
                .filter(msg -> msg instanceof UserMessage)
                .map(msg -> ((UserMessage) msg).getText())
                .reduce((first, second) -> second) // 获取最后一个
                .orElse("");

        if (userQuery.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 使用 LLM 增强查询（只执行一次，在整个 Agent 执行过程中）
        // 简化示例：实际可以使用 RewriteQueryTransformer
        String enhancedQuery = enhanceQuery(userQuery);

        // 如果查询被增强，更新消息列表
        if (!enhancedQuery.equals(userQuery)) {
            List<Message> enhancedMessages = new ArrayList<>();
            // 保留系统消息和其他消息，只替换用户消息
            for (Message msg : messages) {
                if (msg instanceof UserMessage) {
                    enhancedMessages.add(new UserMessage(enhancedQuery));
                } else {
                    enhancedMessages.add(msg);
                }
            }

            // 将增强后的查询存储到 metadata 中，供后续使用
            config.metadata().ifPresent(meta -> {
                meta.put(ENHANCED_QUERY_KEY, enhancedQuery);
            });

            // 返回更新后的消息列表
            return CompletableFuture.completedFuture(Map.of("messages", enhancedMessages));
        }

        return CompletableFuture.completedFuture(Map.of());
    }

    private String enhanceQuery(String query) {
        // 简化示例：实际可以使用 RewriteQueryTransformer 或调用 LLM 进行查询重写
        // 这里只是示例，实际应该调用 LLM 增强查询
        // 例如：使用 RewriteQueryTransformer.builder().chatClientBuilder(...).build().transform(query)
        return query; // 实际实现中会调用 LLM 增强查询
    }
}
