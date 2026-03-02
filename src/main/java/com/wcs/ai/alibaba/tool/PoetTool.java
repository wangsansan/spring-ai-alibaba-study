package com.wcs.ai.alibaba.tool;

import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

public class PoetTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(String subject, ToolContext toolContext) {
        return "a poem about " + subject;
    }
}
