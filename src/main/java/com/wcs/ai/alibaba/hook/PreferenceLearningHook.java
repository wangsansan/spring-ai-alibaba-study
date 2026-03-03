package com.wcs.ai.alibaba.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.chat.messages.Message;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@HookPositions({HookPosition.AFTER_MODEL})
public class PreferenceLearningHook extends ModelHook {

    @Override
    public String getName() {
        return "preference_learning";
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        String userId = (String) config.metadata("user_id").orElse(null);
        if (userId == null) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 提取用户输入
        List<Message> messages = (List<Message>) state.value("messages").orElse(new ArrayList<>());
        if (messages.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }

        Store store = config.store();
        // 加载现有偏好
        Optional<StoreItem> prefsOpt = store.getItem(List.of("user_data"), userId + "_preferences");
        List<String> prefs = new ArrayList<>();
        if (prefsOpt.isPresent()) {
            Map<String, Object> prefsData = prefsOpt.get().getValue();
            prefs = (List<String>) prefsData.getOrDefault("items", new ArrayList<>());
        }

        // 简单的偏好提取（实际应用中使用NLP）
        for (Message msg : messages) {
            String content = msg.getText().toLowerCase();
            if (content.contains("喜欢") || content.contains("偏好")) {
                prefs.add(msg.getText());

                Map<String, Object> prefsData = new HashMap<>();
                prefsData.put("items", prefs);
                StoreItem item = StoreItem.of(List.of("user_data"), userId + "_preferences", prefsData);
                store.putItem(item);

                System.out.println("学习到用户偏好 " + userId + ": " + msg.getText());
            }
        }

        return CompletableFuture.completedFuture(Map.of());
    }
}
