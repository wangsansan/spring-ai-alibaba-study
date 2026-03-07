package com.wcs.ai.alibaba.rag;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.wcs.ai.alibaba.rag.dto.Request;
import com.wcs.ai.alibaba.rag.dto.Response;
import com.wcs.ai.alibaba.rag.hook.QueryEnhancementHook;
import com.wcs.ai.alibaba.rag.interceptor.AnswerValidationInterceptor;
import com.wcs.ai.alibaba.rag.tool.DatabaseQueryTool;
import com.wcs.ai.alibaba.rag.tool.RagSearchTool;
import com.wcs.ai.alibaba.rag.tool.WebSearchTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.function.Function;

@Configuration
public class RagConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 1. 加载文档
        Resource resource = new ClassPathResource("rag/txt/kafka.md");
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.get();

        // 2. 分割文档为块
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(documents);
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(chunks);
        return vectorStore;
    }

    @Bean
    public RagSearchTool ragSearchTool(VectorStore vectorStore) {
        return new RagSearchTool(vectorStore);
    }

    @Bean
    public WebSearchTool webSearchTool() {
        return new WebSearchTool();
    }

    @Bean
    public DatabaseQueryTool databaseQueryTool() {
        return new DatabaseQueryTool();
    }

    @Bean
    public ToolCallback searchCallback(RagSearchTool searchTool) {
        return FunctionToolCallback.builder("search_documents",
                        (Function<Request, Response>)
                                request -> searchTool.search(request))
                .description("搜索文档以查找相关信息")
                .inputType(Request.class)
                .build();
    }

    @Bean
    public ToolCallback webSearchCallback(WebSearchTool webSearchTool) {
        return FunctionToolCallback.builder("web_search",
                        webSearchTool::search)
                .description("搜索互联网以获取最新信息")
                .inputType(Request.class)
                .build();
    }

    @Bean
    public ToolCallback databaseQueryCallback(DatabaseQueryTool databaseQueryTool) {
        return FunctionToolCallback.builder("database_query",
                        databaseQueryTool::query)
                .description("搜索互联网以获取最新信息")
                .inputType(Request.class)
                .build();
    }

    @Bean
    public ReactAgent ragAgent(ChatModel chatModel, VectorStore vectorStore) {
        return ReactAgent.builder()
                .name("rag_agent")
                .model(chatModel)
//                .hooks(new RAGMessagesHook(vectorStore))
                .interceptors(new RAGModelInterceptor(vectorStore))
                .build();
    }

    @Bean
    public ReactAgent agenticRagAgent(ChatModel chatModel, ToolCallback searchCallback) {
        return ReactAgent.builder()
                .name("rag_agent")
                .model(chatModel)
                .instruction("你是一个智能助手。当需要查找信息时，使用search_documents工具。" +
                        "基于检索到的信息回答用户的问题，并引用相关片段。")
                .tools(searchCallback)
                .build();
    }

    @Bean
    public ReactAgent multiSourceAgent(ChatModel chatModel, ToolCallback webSearchCallback, ToolCallback databaseQueryCallback, ToolCallback searchCallback) {
        return ReactAgent.builder()
                .name("multi_source_rag_agent")
                .model(chatModel)
                .instruction("你可以访问多个信息源：" +
                                "1. web_search - 用于最新的互联网信息 " +
                                "2. database_query - 用于内部数据 " +
                                "3. document_search - 用于文档库 " +
                                "根据问题选择最合适的工具。")
                .tools(webSearchCallback, databaseQueryCallback, searchCallback)
                .build();
    }

    @Bean
    public ReactAgent hybridRAGAgent(ChatModel chatModel, ToolCallback searchCallback, ToolCallback webSearchCallback) {
        return ReactAgent.builder()
                .name("hybrid_rag_agent")
                .model(chatModel)
                .instruction("""
                  你是一个智能助手，可以访问多个信息源来回答问题。
                  
                  使用工具时：
                  1. 优先使用 document_search 搜索文档库
                  2. 如果需要最新信息，使用 web_search
                  3. 基于检索到的信息生成准确、完整的答案
                  4. 如果信息不足，可以多次调用工具
                  """)
                .tools(searchCallback, webSearchCallback)
                .hooks(new QueryEnhancementHook(chatModel))
                .interceptors(new AnswerValidationInterceptor(chatModel))
                .build();
    }





}
