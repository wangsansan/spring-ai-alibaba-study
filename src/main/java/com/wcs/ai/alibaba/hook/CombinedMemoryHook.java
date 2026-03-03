package com.wcs.ai.alibaba.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@HookPositions({HookPosition.BEFORE_MODEL})
public class CombinedMemoryHook extends ModelHook {

    @Override
    public String getName() {
        return "combined_memory_hook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        Optional<Object> userIdOpt = config.metadata("user_id");
        if (userIdOpt.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }
        String userId = (String) userIdOpt.get();

        Store memoryStore = config.store();
        // 从长期记忆加载
        Optional<StoreItem> profileOpt = memoryStore.getItem(List.of("profiles"), userId);
        if (profileOpt.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        Map<String, Object> profile = profileOpt.get().getValue();
        String contextInfo = String.format("长期记忆：用户 %s, 职业: %s",
                profile.get("name"), profile.get("occupation"));

        // 获取消息列表
        List<Message> messages = (List<Message>) state.value("messages").orElse(new ArrayList<>());
        List<Message> newMessages = new ArrayList<>();

        // 查找是否已存在 SystemMessage
        SystemMessage existingSystemMessage = null;
        int systemMessageIndex = -1;
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (msg instanceof SystemMessage) {
                existingSystemMessage = (SystemMessage) msg;
                systemMessageIndex = i;
                break;
            }
        }

        // 如果找到 SystemMessage，更新它；否则创建新的
        SystemMessage enhancedSystemMessage;
        if (existingSystemMessage != null) {
            // 更新现有的 SystemMessage
            enhancedSystemMessage = new SystemMessage(
                    existingSystemMessage.getText() + " " + contextInfo
            );
        } else {
            // 创建新的 SystemMessage
            enhancedSystemMessage = new SystemMessage(contextInfo);
        }

        // 构建新的消息列表
        if (systemMessageIndex >= 0) {
            // 如果找到了 SystemMessage，替换它
            for (int i = 0; i < messages.size(); i++) {
                if (i == systemMessageIndex) {
                    newMessages.add(enhancedSystemMessage);
                } else {
                    newMessages.add(messages.get(i));
                }
            }
        } else {
            // 如果没有找到 SystemMessage，在开头添加新的
            newMessages.add(enhancedSystemMessage);
            newMessages.addAll(messages);
        }

        return CompletableFuture.completedFuture(Map.of("messages", newMessages));
    }
}
