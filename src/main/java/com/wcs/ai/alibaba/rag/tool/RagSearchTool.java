package com.wcs.ai.alibaba.rag.tool;

import com.wcs.ai.alibaba.rag.dto.Request;
import com.wcs.ai.alibaba.rag.dto.Response;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.stream.Collectors;

public class RagSearchTool {

    private VectorStore vectorStore;

    public RagSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Response search(Request request) {
        System.out.println("==================use search tool===============");
        // 从向量存储检索相关文档
        List<Document> docs = vectorStore.similaritySearch(request.query());

        // 合并文档内容
        String combinedContent = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(""));

        System.out.println("==================search data  begin===============");
        System.out.println(combinedContent);
        System.out.println("==================search data end===============");


        return new Response(combinedContent);
    }

}
