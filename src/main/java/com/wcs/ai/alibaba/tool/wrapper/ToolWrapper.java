package com.wcs.ai.alibaba.tool.wrapper;

import com.wcs.ai.alibaba.tool.DateTimeTools;
import lombok.SneakyThrows;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class ToolWrapper {

    @SneakyThrows
    public ToolCallback wrapMethod() {
        Method getCurrentDateTime = ReflectionUtils.findMethod(DateTimeTools.class, "getCurrentDateTime");
        return MethodToolCallback.builder()
                .toolDefinition(
                        ToolDefinitions.builder(getCurrentDateTime)
                                .description("Get the current date and time in the user's timezone")
                                .build()
                )
                .toolMethod(getCurrentDateTime)
                .toolObject(new DateTimeTools())
                .build();
    }

}
