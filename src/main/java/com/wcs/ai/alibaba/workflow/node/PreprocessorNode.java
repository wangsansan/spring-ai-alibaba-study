package com.wcs.ai.alibaba.workflow.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class PreprocessorNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input", StringUtils.EMPTY);
        String cleaned = input.trim();
        return Map.of("cleaned_input", cleaned);
    }
}
