package com.wcs.ai.alibaba.utils;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HITLHelper 单元测试
 * 测试人工介入（Human-In-The-Loop）中断处理的工具类
 */
class HITLHelperTest {

    @Test
    void testApproveAll() {
        // 准备测试数据 - 使用 builder 复制现有 feedback 并设置 result
        InterruptionMetadata.ToolFeedback feedback1 = createToolFeedback("get_weather", "{\"city\": \"北京\"}", "查询天气工具");
        InterruptionMetadata.ToolFeedback feedback2 = createToolFeedback("execute_sql", "{\"query\": \"SELECT * FROM users\"}", "执行 SQL 工具");

        InterruptionMetadata interruptionMetadata = InterruptionMetadata.builder()
                .nodeId("test_node")
                .state(Map.of("input", "test input"))
                .toolFeedbacks(List.of(feedback1, feedback2))
                .build();

        // 执行测试
        InterruptionMetadata result = HITLHelper.approveAll(interruptionMetadata);

        // 验证结果
        assertNotNull(result);
        assertEquals("test_node", result.node());
        assertEquals(2, result.toolFeedbacks().size());

        // 验证所有工具都被批准
        result.toolFeedbacks().forEach(feedback -> {
            assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED, feedback.result());
        });
    }

    @Test
    void testRejectAll() {
        // 准备测试数据
        InterruptionMetadata.ToolFeedback feedback = createToolFeedback("delete_data", "{\"table\": \"users\"}", "删除数据工具");

        InterruptionMetadata interruptionMetadata = InterruptionMetadata.builder()
                .nodeId("test_node")
                .state(Map.of("input", "test input"))
                .toolFeedbacks(List.of(feedback))
                .build();

        // 执行测试
        String rejectReason = "操作不安全，拒绝执行";
        InterruptionMetadata result = HITLHelper.rejectAll(interruptionMetadata, rejectReason);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.toolFeedbacks().size());

        InterruptionMetadata.ToolFeedback resultFeedback = result.toolFeedbacks().get(0);
        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED, resultFeedback.result());
        assertEquals(rejectReason, resultFeedback.description());
    }

    @Test
    void testEditTool() {
        // 准备测试数据
        InterruptionMetadata.ToolFeedback feedback1 = createToolFeedback("execute_sql", "{\"query\": \"DELETE FROM users WHERE 1=1\"}", "执行 SQL 工具");
        InterruptionMetadata.ToolFeedback feedback2 = createToolFeedback("get_weather", "{\"city\": \"上海\"}", "查询天气工具");

        InterruptionMetadata interruptionMetadata = InterruptionMetadata.builder()
                .nodeId("test_node")
                .state(Map.of("input", "test input"))
                .toolFeedbacks(List.of(feedback1, feedback2))
                .build();

        // 执行测试 - 编辑 SQL 工具的参数
        String newArguments = "{\"query\": \"SELECT * FROM users LIMIT 10\"}";
        InterruptionMetadata result = HITLHelper.editTool(interruptionMetadata, "execute_sql", newArguments);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.toolFeedbacks().size());

        // 验证被编辑的工具
        InterruptionMetadata.ToolFeedback editedFeedback = result.toolFeedbacks().stream()
                .filter(f -> "execute_sql".equals(f.name()))
                .findFirst()
                .orElseThrow();

        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED, editedFeedback.result());
        assertEquals(newArguments, editedFeedback.arguments());

        // 验证其他工具被批准
        InterruptionMetadata.ToolFeedback otherFeedback = result.toolFeedbacks().stream()
                .filter(f -> "get_weather".equals(f.name()))
                .findFirst()
                .orElseThrow();

        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED, otherFeedback.result());
    }

    @Test
    void testEditToolNotFound() {
        // 准备测试数据
        InterruptionMetadata.ToolFeedback feedback = createToolFeedback("get_weather", "{\"city\": \"北京\"}", "查询天气工具");

        InterruptionMetadata interruptionMetadata = InterruptionMetadata.builder()
                .nodeId("test_node")
                .state(Map.of("input", "test input"))
                .toolFeedbacks(List.of(feedback))
                .build();

        // 执行测试 - 编辑不存在的工具
        String newArguments = "{\"query\": \"SELECT 1\"}";
        InterruptionMetadata result = HITLHelper.editTool(interruptionMetadata, "non_existent_tool", newArguments);

        // 验证结果 - 所有工具应该保持批准状态
        assertNotNull(result);
        assertEquals(1, result.toolFeedbacks().size());
        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED, result.toolFeedbacks().get(0).result());
    }

    /**
     * 辅助方法：创建 ToolFeedback 测试数据
     * 使用 builder 从空对象开始构建
     */
    private InterruptionMetadata.ToolFeedback createToolFeedback(String name, String arguments, String description) {
        return InterruptionMetadata.ToolFeedback.builder()
                .name(name)
                .arguments(arguments)
                .description(description)
                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.PENDING)
                .build();
    }
}
