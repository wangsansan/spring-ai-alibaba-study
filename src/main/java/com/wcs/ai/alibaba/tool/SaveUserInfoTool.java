package com.wcs.ai.alibaba.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.wcs.ai.alibaba.tool.dto.MemoryResponse;
import com.wcs.ai.alibaba.tool.dto.SaveMemoryRequest;
import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

public class SaveUserInfoTool implements BiFunction<SaveMemoryRequest, ToolContext, MemoryResponse> {

    @Override
    public MemoryResponse apply(SaveMemoryRequest request, ToolContext context) {
        RunnableConfig runnableConfig = (RunnableConfig) context.getContext().get("_AGENT_CONFIG_");
        Store store = runnableConfig.store();
        StoreItem item = StoreItem.of(request.namespace(), request.key(), request.value());
        store.putItem(item);
        return new MemoryResponse("成功保存用户信息", request.value());
    }
}
