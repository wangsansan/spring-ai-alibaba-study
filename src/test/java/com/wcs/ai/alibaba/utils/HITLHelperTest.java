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
        // 准备测试数据
        InterruptionMetadata.ToolFeedback feedback1 = InterruptionMetadata.ToolFeedback.builder()
                .name("get_weather")
                .arguments("{\"city\": \"北京\"}")
                .description("查询天气工具")
                .build();

        InterruptionMetadata.ToolFeedback feedback2 = InterruptionMetadata.ToolFeedback.builder()
                .name("execute_sql")
                .arguments("{\"query\": \"SELECT * FROM users\"}")
                .description("执行 SQL 工具")
                .build();

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
        InterruptionMetadata.ToolFeedback feedback = InterruptionMetadata.ToolFeedback.builder()
                .name("delete_data")
                .arguments("{\"table\": \"users\"}")
                .description("删除数据工具")
                .build();

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
        InterruptionMetadata.ToolFeedback feedback1 = InterruptionMetadata.ToolFeedback.builder()
                .name("execute_sql")
                .arguments("{\"query\": \"DELETE FROM users WHERE 1=1\"}")
                .description("执行 SQL 工具")
                .build();

        InterruptionMetadata.ToolFeedback feedback2 = InterruptionMetadata.ToolFeedback.builder()
                .name("get_weather")
                .arguments("{\"city\": \"上海\"}")
                .description("查询天气工具")
                .build();

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
                .filter(f -> f.name().equals("execute_sql"))
                .findFirst()
                .orElseThrow();

        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED, editedFeedback.result());
        assertEquals(newArguments, editedFeedback.arguments());

        // 验证其他工具被批准
        InterruptionMetadata.ToolFeedback otherFeedback = result.toolFeedbacks().stream()
                .filter(f -> f.name().equals("get_weather"))
                .findFirst()
                .orElseThrow();

        assertEquals(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED, otherFeedback.result());
    }

    @Test
    void testEditToolNotFound() {
        // 准备测试数据
        InterruptionMetadata.ToolFeedback feedback = InterruptionMetadata.ToolFeedback.builder()
                .name("get_weather")
                .arguments("{\"city\": \"北京\"}")
                .description("查询天气工具")
                .build();

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
}
