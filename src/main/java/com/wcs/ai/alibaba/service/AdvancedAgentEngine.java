package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AdvancedAgentEngine {

    @Resource
    private ReactAgent memoryAgent;

    @Resource
    private ReactAgent saveMemoryAgent;

    @Resource
    private ReactAgent modelMemoryAgent;

    @Resource
    private ReactAgent combinedMemoryAgent;

    @Resource
    private ReactAgent sessionAgent;

    @Resource
    private ReactAgent learningAgent;

    @SneakyThrows
    public void callWithStore() {
        // 创建内存存储
        MemoryStore store = new MemoryStore();
        // 向存储中写入示例数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "张三");
        userData.put("language", "中文");

        StoreItem userItem = StoreItem.of(List.of("users"), "user_123", userData);
        store.putItem(userItem);

        // 运行Agent
        RunnableConfig config = RunnableConfig.builder()
                .threadId("session_001")
                .addMetadata("user_id", "user_123")
                .store(store)
                .build();

        AssistantMessage assistantMessage = memoryAgent.call("查询用户信息，namespace=['users'], key='user_123'", config);
        System.out.println(JSON.toJSONString(assistantMessage));
    }

    @SneakyThrows
    public void saveDataToStore() {
        // 创建内存存储
        MemoryStore store = new MemoryStore();
        RunnableConfig config = RunnableConfig.builder()
                .threadId("session_001")
                .addMetadata("user_id", "user_123")
                .store(store)
                .build();

        // 运行Agent
        saveMemoryAgent.invoke(
                "我叫张三，请保存我的信息。使用 saveUserInfo 工具，namespace=['users'], key='user_abc', value={'name': '王三'}",
                config
        );

        // 可以直接访问存储获取值
        Optional<StoreItem> savedItem = store.getItem(List.of("users"), "user_abc");
        if (savedItem.isPresent()) {
            Map<String, Object> savedValue = savedItem.get().getValue();
            System.out.println("fetched data:" + JSON.toJSONString(savedValue));
        }
    }

    @SneakyThrows
    public void callWithModelStore() {
        // 创建内存存储
        MemoryStore memoryStore = new MemoryStore();

        // 模拟数据，预先填充用户画像
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", "王小明");
        profileData.put("age", 28);
        profileData.put("email", "wang@example.com");
        profileData.put("preferences", List.of("喜欢咖啡", "喜欢阅读"));

        StoreItem profileItem = StoreItem.of(List.of("user_profiles"), "user_001", profileData);
        memoryStore.putItem(profileItem);

        RunnableConfig config = RunnableConfig.builder()
                .threadId("session_001")
                .addMetadata("user_id", "user_001")
                .store(memoryStore)
                .build();

        // Agent会自动加载用户画像信息
        Optional<OverAllState> result = modelMemoryAgent.invoke("请介绍一下我的信息。", config);
        result.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
    }

    @SneakyThrows
    public void callWithCombinedMemory() {
        // 创建记忆存储
        MemoryStore memoryStore = new MemoryStore();
        // 设置长期记忆
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", "李工程师");
        userProfile.put("occupation", "软件工程师");

        StoreItem profileItem = StoreItem.of(List.of("profiles"), "user_002", userProfile);
        memoryStore.putItem(profileItem);

        RunnableConfig config = RunnableConfig.builder()
                .threadId("combined_thread")
                .addMetadata("user_id", "user_002")
                .store(memoryStore)
                .build();

        // 短期记忆：在对话中记住
        combinedMemoryAgent.invoke("我今天在做一个 Spring 项目。", config);

        // 提出需要同时使用两种记忆的问题
        Optional<OverAllState> invoke = combinedMemoryAgent.invoke("根据我的职业和今天的工作，给我一些建议。", config);
        // 响应会同时使用长期记忆（职业）和短期记忆（Spring项目）
        invoke.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
    }

    /**
     * 通过memoryStore来保存长期记忆
     * - 在两个不同的 RunnableConfig 中进行数据获取没有问题，其实就是数据持久化了
     */
    @SneakyThrows
    public void callCrossSession() {
        // 创建记忆存储和工具
        MemoryStore memoryStore = new MemoryStore();
        // 会话1：保存信息
        RunnableConfig session1 = RunnableConfig.builder()
                .threadId("session_morning")
                .addMetadata("user_id", "user_003")
                .store(memoryStore)
                .build();

        sessionAgent.invoke(
                "记住我的密码是 secret123。用 saveMemory 保存，namespace=['credentials'], key='user_003_password', value={'password': 'secret123'}。",
                session1
        );

        CompletableFuture.runAsync(() -> {
            // 会话2：检索信息（不同的线程，同一用户）
            RunnableConfig session2 = RunnableConfig.builder()
                    .threadId("session_afternoon")
                    .addMetadata("user_id", "user_003")
                    .store(memoryStore)
                    .build();

            try {
                Optional<OverAllState> invoke = sessionAgent.invoke(
                        "我的密码是什么？用 getMemory 获取，namespace=['credentials'], key='user_003_password'。",
                        session2
                );
                invoke.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
                // 长期记忆在不同会话间持久化
            } catch (Exception e) {
                log.error("sessionAgent.invoke error", e);
            }
        });
    }

    @SneakyThrows
    public void callWithPreferenceLearning() {
        MemoryStore memoryStore = new MemoryStore();

        RunnableConfig config = RunnableConfig.builder()
                .threadId("learning_thread")
                .addMetadata("user_id", "user_004")
                .store(memoryStore)
                .build();

        // 用户表达偏好
        learningAgent.invoke("我喜欢喝绿茶。", config);
        learningAgent.invoke("我偏好早上运动。", config);

        // 验证偏好已被存储
        Optional<StoreItem> savedPrefs = memoryStore.getItem(List.of("user_data"), "user_004_preferences");
        if (savedPrefs.isPresent()) {
            // 偏好应该被保存到长期记忆中
            System.out.println(JSON.toJSONString(savedPrefs.get().getValue()));
        }
    }

}
