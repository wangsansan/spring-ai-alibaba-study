package com.wcs.ai.alibaba.tool.dto;

import java.util.List;
import java.util.Map;

public record SaveMemoryRequest(List<String> namespace, String key, Map<String, Object> value) {
}
