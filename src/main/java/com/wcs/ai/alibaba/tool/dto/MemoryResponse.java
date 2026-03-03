package com.wcs.ai.alibaba.tool.dto;

import java.util.Map;

public record MemoryResponse(String message, Map<String, Object> value) {
}
