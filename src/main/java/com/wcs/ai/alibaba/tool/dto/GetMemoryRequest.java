package com.wcs.ai.alibaba.tool.dto;

import java.util.List;

public record GetMemoryRequest(List<String> namespace, String key) {
}
