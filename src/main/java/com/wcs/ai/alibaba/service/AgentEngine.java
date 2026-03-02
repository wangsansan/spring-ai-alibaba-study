package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentEngine {

    @Resource
    private ReactAgent weatherPunAgent;

    @Resource
    private ReactAgent approvalAgent;

    @Resource
    private ReactAgent poetAgent;

    @SneakyThrows
    public void callAgent() {
        String threadId = UUID.randomUUID().toString();

        // threadId 是给定对话的唯一标识符
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "1").build();

        System.out.println("=========================================");
        // 第一次调用
        AssistantMessage response = weatherPunAgent.call("what is the weather outside?", runnableConfig);
        System.out.println(response.getText());

        // 注意我们可以使用相同的 threadId 继续对话
        response = weatherPunAgent.call("thank you!", runnableConfig);
        System.out.println(response.getText());
    }

    /**
     * 使用工具需要人工介入审批，响应中断，需要审查，approve
     */
    @SneakyThrows
    public void approvalGetWeather() {
        // 人工介入利用检查点机制。
        // 你必须提供线程ID以将执行与会话线程关联，
        // 以便可以暂停和恢复对话（人工审查所需）。
        String threadId = "user-session-123";
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        Optional<NodeOutput> result = approvalAgent.invokeAndGetOutput("北京天气如何？", config);
        // 检查是否返回了中断，
        // java17的写法，当instanceof InterruptionMetadata 为true时，
        // - 自动将result.get()转化成InterruptionMetadata类型，并赋值给interruptionMetadata对象
        if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {

            // 中断包含需要审查的工具反馈
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks =
                    interruptionMetadata.toolFeedbacks();

            for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                System.out.println("工具: " + feedback.getName());
                System.out.println("参数: " + feedback.getArguments());
                System.out.println("描述: " + feedback.getDescription());
            }

            // 示例输出:
            // 工具: execute_sql
            // 参数: {"query": "DELETE FROM records WHERE created_at < NOW() - INTERVAL '30 days';"}
            // 描述: SQL执行操作需要审批
        }
    }

    @SneakyThrows
    public void createPoemWithApproval() {
        String threadId = "user-session-001";
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        // 4. 第一次调用 - 触发中断
        System.out.println("=== 第一次调用：期望中断 ===");
        Optional<NodeOutput> result = poetAgent.invokeAndGetOutput(
                "春天",
                config
        );

        // 5. 检查中断并处理
        if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {

            System.out.println("检测到中断，需要人工审批");
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks =
                    interruptionMetadata.toolFeedbacks();

            for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                System.out.println("工具: " + feedback.getName());
                System.out.println("参数: " + feedback.getArguments());
                System.out.println("描述: " + feedback.getDescription());
            }

            // 6. 模拟人工决策（这里选择批准）
            InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                    .nodeId(interruptionMetadata.node())
                    .state(interruptionMetadata.state());

            toolFeedbacks.forEach(toolFeedback -> {
                InterruptionMetadata.ToolFeedback approvedFeedback =
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build();
                feedbackBuilder.addToolFeedback(approvedFeedback);
            });

            InterruptionMetadata approvalMetadata = feedbackBuilder.build();

            // 7. 第二次调用 - 使用人工反馈恢复执行
            System.out.println("=== 第二次调用：使用批准决策恢复 ===");
                    RunnableConfig resumeConfig = RunnableConfig.builder()
                            .threadId(threadId)
                            .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                            .build();

            Optional<NodeOutput> finalResult = poetAgent.invokeAndGetOutput("春天", resumeConfig);

            if (finalResult.isPresent()) {
                System.out.println("执行完成");
                System.out.println("最终结果: " + finalResult.get());
            }
        }
    }



}
