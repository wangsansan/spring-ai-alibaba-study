package com.wcs.ai.alibaba.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.wcs.ai.alibaba.tool.dto.GetMemoryRequest;
import com.wcs.ai.alibaba.tool.dto.MemoryResponse;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class GetUserInfoTool implements BiFunction<GetMemoryRequest, ToolContext, MemoryResponse> {

    @Override
    public MemoryResponse apply(GetMemoryRequest request, ToolContext toolContext) {
        // 注意该key，通过debug从上下文里拿到的
        RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get("_AGENT_CONFIG_");
        Store store = runnableConfig.store();
        Optional<StoreItem> itemOpt = store.getItem(request.namespace(), request.key());
        if (itemOpt.isPresent()) {
            Map<String, Object> value = itemOpt.get().getValue();
            return new MemoryResponse("找到用户信息", value);
        }
        return new MemoryResponse("未找到用户", Map.of());
    }
}
