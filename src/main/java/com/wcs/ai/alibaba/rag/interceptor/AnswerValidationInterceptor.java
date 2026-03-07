package com.wcs.ai.alibaba.rag.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;

public class AnswerValidationInterceptor extends ModelInterceptor {
    private  ChatModel chatModel;
    private static final double MIN_CONFIDENCE = 0.7;

    public AnswerValidationInterceptor(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        // 先调用模型生成答案
        ModelResponse response = handler.call(request);

        // 验证答案质量（简化示例）
        AssistantMessage answer = response.getChatResponse().getResult().getOutput();
        boolean isValid = validateAnswer(answer.getText(), request);

        if (!isValid) {
            // 如果答案质量不足，可以添加提示要求重新生成
            SystemMessage validationPrompt = new SystemMessage(
                    "请重新检查你的答案，确保基于提供的上下文信息，并且准确完整。"
            );

            ModelRequest retryRequest = ModelRequest.builder(request)
                    .systemMessage(validationPrompt)
                    .build();

            // 可以选择重试或返回当前答案
            return handler.call(retryRequest);
        }

        return response;
    }

    private boolean validateAnswer(String answer, ModelRequest request) {
        // 简化示例：实际可以使用 LLM 验证答案与上下文的一致性
        // 检查答案长度、是否包含关键信息等
        return answer != null && answer.length() > 20; // 简单验证
    }

    @Override
    public String getName() {
        return "answer_validation";
    }
}
