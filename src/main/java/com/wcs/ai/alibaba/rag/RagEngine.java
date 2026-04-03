package com.wcs.ai.alibaba.rag;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RagEngine {

    @Resource
    private ReactAgent ragAgent;

    @Resource
    private ReactAgent agenticRagAgent;

    @Resource
    private ReactAgent multiSourceAgent;

    @Resource
    private ReactAgent hybridRAGAgent;

    @Resource
    private ChatClient chatClient;

    @Resource
    private RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    @SneakyThrows
    public void call() {
        // 调用 Agent
        AssistantMessage response = ragAgent.call("kafka的可靠性是怎么做的？");
        System.out.println("答案: " + response.getText());
    }

    @SneakyThrows
    public void agenticCall() {
        Optional<OverAllState> invoke = agenticRagAgent.invoke("Spring AI Alibaba支持哪些向量数据库？");
        System.out.println("******************call result  begin***************");
        invoke.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
        System.out.println("******************call result  end***************");
        Optional<OverAllState> invoke1 = agenticRagAgent.invoke("kafka的producer是怎么保证消息的顺序性的？");
        System.out.println("******************call result  begin***************");
        invoke1.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
        System.out.println("******************call result  end***************");
        Optional<OverAllState> invoke2 = agenticRagAgent.invoke("kafka的可靠性是怎么保证的？");
        System.out.println("******************call result  begin***************");
        invoke2.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
        System.out.println("******************call result  end***************");
    }

    @SneakyThrows
    public void adviceCall(String question) {
        String systemInfo = """
                    你是一个kafka技术专家，按照我给出的问题，给出相关答案
                """;
        System.out.println(chatClient
                .prompt()
                .system(systemInfo)
                .user(question)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content());
    }

    @SneakyThrows
    public void callWithMultiSourceAgent() {
        Optional<OverAllState> invoke = multiSourceAgent.invoke("比较kafka和RocketMQ的区别");
        invoke.ifPresent(overAllState -> System.out.println(JSON.toJSONString(overAllState.data())));
    }

    @SneakyThrows
    public void hybridCall() {
        AssistantMessage response = hybridRAGAgent.call("kafka的可靠性是怎么保证的？");
        System.out.println("答案: " + response.getText());
    }

}
