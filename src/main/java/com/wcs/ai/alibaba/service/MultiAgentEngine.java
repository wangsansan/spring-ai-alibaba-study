package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import com.wcs.ai.alibaba.agent.flow.ConditionalAgent;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MultiAgentEngine {

    @Resource
    private SequentialAgent blogAgent;

    @Resource
    private ParallelAgent parallelAgent;

    @Resource
    private LlmRoutingAgent routingAgent;

    @Resource
    private SequentialAgent sequentialAgent;

    @Resource
    private SequentialAgent complexAgent;

    @Resource
    private ConditionalAgent conditionalAgent;

    @SneakyThrows
    public void writeBlog() {
        // 使用
        Optional<OverAllState> result = blogAgent.invoke("帮我写一个100字左右的散文");

        if (result.isPresent()) {
            OverAllState state = result.get();

            // 访问第一个Agent的输出
            state.value("article").ifPresent(article -> {
                if (article instanceof AssistantMessage) {
                    System.out.println("原始文章: " + ((AssistantMessage) article).getText());
                }
            });

            // 访问第二个Agent的输出
            state.value("reviewed_article").ifPresent(reviewedArticle -> {
                if (reviewedArticle instanceof AssistantMessage) {
                    System.out.println("评审后文章: " + ((AssistantMessage) reviewedArticle).getText());
                }
            });
        }
    }

    @SneakyThrows
    public void createByParallelAgent() {
        // 使用
        Optional<OverAllState> result = parallelAgent.invoke("以'西湖'为主题");

        if (result.isPresent()) {
            OverAllState state = result.get();

            // 访问各个Agent的输出
            state.value("prose_result").ifPresent(r ->
                    System.out.println("散文: " + r));
            state.value("poem_result").ifPresent(r ->
                    System.out.println("诗歌: " + r));
            state.value("summary_result").ifPresent(r ->
                    System.out.println("总结: " + r));

            // 访问合并后的结果
            state.value("merged_results").ifPresent(r ->
                    System.out.println("合并结果: " + r));
        }
    }

    /**
     * 执行日志能看出来routingAgent路由哪个agent了
     */
    @SneakyThrows
    public void createByRoutingAgent() {
        // 使用 - LLM会自动选择最合适的Agent
        Optional<OverAllState> result1 = routingAgent.invoke("帮我写一篇关于春天的散文");
        // LLM会路由到 writerAgent

        Optional<OverAllState> result2 = routingAgent.invoke("请帮我修改这篇文章：春天来了，花开了。");
        // LLM会路由到 reviewerAgent

        Optional<OverAllState> result3 = routingAgent.invoke("请将以下内容翻译成英文：春暖花开");
        // LLM会路由到 translatorAgent
    }

    @SneakyThrows
    public void createByComplexAgent() {
        Optional<OverAllState> result = sequentialAgent.invoke("帮我写一篇关于春天的短文，然后翻译成英文");
        result.flatMap(state -> state.value("translated_article")).ifPresent(r ->
                System.out.println("翻译后的文章: " + r));
    }

    @SneakyThrows
    public void createDataByComplexAgent() {
        Optional<OverAllState> result = complexAgent.invoke("研究AI技术趋势并生成HTML报告");

        result.ifPresent(state -> {
            // 访问各个Agent的输出
            state.value("web_data").ifPresent(r ->
                    System.out.println("web搜索: " + r));
            state.value("create_data").ifPresent(r ->
                    System.out.println("营销文案: " + r));
            state.value("research_data").ifPresent(r ->
                    System.out.println("总结: " + r));
            state.value("analysis_result").ifPresent(r ->
                    System.out.println("分析结果: " + r));
            state.value("html_report").ifPresent(r ->
                    System.out.println("HTML报告: " + r));
        });
    }

    @SneakyThrows
    public void callConditional() {
        // 使用
        Optional<OverAllState> result1 = conditionalAgent.invoke("这是一个紧急问题需要立即处理");
        result1.ifPresent(overAllState -> {
            System.out.println("=======urgent invoke=========");
            System.out.println("=======urgent result=========");
            System.out.println(overAllState.value("urgent_result"));
            System.out.println("=======normal result=========");
            System.out.println(overAllState.value("normal_result"));
        });
        // 会路由到 urgentAgent
        Optional<OverAllState> result2 = conditionalAgent.invoke("请帮我分析一下这个问题");
        result2.ifPresent(overAllState -> {
            System.out.println("=======normal invoke=========");
            System.out.println("=======urgent result=========");
            System.out.println(overAllState.value("urgent_result"));
            System.out.println("=======normal result=========");
            System.out.println(overAllState.value("normal_result"));
        });
    }

}
