package com.wcs.ai.alibaba.rag.tool;

import com.wcs.ai.alibaba.rag.dto.Request;
import com.wcs.ai.alibaba.rag.dto.Response;

public class DatabaseQueryTool {

    public Response query(Request request) {
        System.out.println("=================数据库搜索");
        return new Response("从数据库查询到的信息: " + request.query());
    }

}
