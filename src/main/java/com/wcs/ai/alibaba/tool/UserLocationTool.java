package com.wcs.ai.alibaba.tool;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Objects;
import java.util.function.BiFunction;

public class UserLocationTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(@ToolParam(description = "User query") String query,
                        ToolContext toolContext) {
        // 从上下文中获取用户信息
        String userId = "";
        if (toolContext != null && toolContext.getContext() != null) {
            Object userId1 = toolContext.getContext().get("user_id");
            if (Objects.nonNull(userId1)) {
                userId = userId1.toString();
            }
        }
        if (StringUtils.isBlank(userId)) {
            userId = "1";
        }
        return "1".equals(userId) ? "Florida" : "San Francisco";
    }
}
