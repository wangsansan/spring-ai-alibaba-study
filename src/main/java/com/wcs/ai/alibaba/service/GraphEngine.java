package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.fastjson.JSON;
import com.wcs.ai.alibaba.utils.HITLHelper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GraphEngine {

    @Resource
    private CompiledGraph compiledGraph;

    @SneakyThrows
    public void runWorkflow() {
        // 9. 执行工作流并处理中断
        String threadId = "workflow-hilt-001";
        Map<String, Object> input = Map.of("input", "请解释量子计算的基本原理");

        // 第一次调用 - 可能触发中断
        Optional<NodeOutput> nodeOutputOptional = compiledGraph.invokeAndGetOutput(
                input,
                RunnableConfig.builder().threadId(threadId).build()
        );

        // 检查是否发生中断
        if (nodeOutputOptional.isPresent()
                && nodeOutputOptional.get() instanceof InterruptionMetadata interruptionMetadata) {

            System.out.println("工作流被中断，等待人工审核。");
            System.out.println("中断节点: " + interruptionMetadata.node());

            InterruptionMetadata approvalMetadata = HITLHelper.approveAll(interruptionMetadata);
            // InterruptionMetadata approvalMetadata = rawApprove(interruptionMetadata)

            // 使用批准决策恢复执行
            RunnableConfig resumableConfig = RunnableConfig.builder()
                    .threadId(threadId) // 相同的线程ID
                    .addHumanFeedback(approvalMetadata)
                    .build();

            // 恢复工作流执行（传入空Map，因为状态已保存在检查点中）
            nodeOutputOptional = compiledGraph.invokeAndGetOutput(Map.of(), resumableConfig);

            if (nodeOutputOptional.isPresent()) {
                System.out.println("执行完成");
                System.out.println("最终结果: " + nodeOutputOptional.get());
            }
        } else {
            System.out.println("结果：" + JSON.toJSONString(nodeOutputOptional.get()));
        }
    }

    private InterruptionMetadata rawApprove(InterruptionMetadata interruptionMetadata) {
        List<InterruptionMetadata.ToolFeedback> feedbacks = interruptionMetadata.toolFeedbacks();

        // 显示所有需要审批的工具调用
        for (InterruptionMetadata.ToolFeedback feedback : feedbacks) {
            System.out.println("工具名称: " + feedback.getName());
            System.out.println("工具参数: " + feedback.getArguments());
            System.out.println("工具描述: " + feedback.getDescription());
        }

        // 构建人工反馈（批准所有工具调用）
        InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());

        feedbacks.forEach(toolFeedback -> {
            feedbackBuilder.addToolFeedback(
                    InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                            .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                            .build()
            );
        });

        return feedbackBuilder.build();
    }

}
