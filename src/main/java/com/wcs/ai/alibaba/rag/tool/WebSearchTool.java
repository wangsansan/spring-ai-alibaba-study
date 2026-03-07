package com.wcs.ai.alibaba.rag.tool;

import com.wcs.ai.alibaba.rag.dto.Request;
import com.wcs.ai.alibaba.rag.dto.Response;

public class WebSearchTool {

    public Response search(Request request) {
        System.out.println("===============网络搜索");
        return new Response("从网络搜索到的信息: " + request.query());
    }

}
