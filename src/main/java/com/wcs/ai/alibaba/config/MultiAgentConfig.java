package com.wcs.ai.alibaba.config;

import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import com.wcs.ai.alibaba.agent.flow.ConditionalAgent;
import com.wcs.ai.alibaba.strategy.CustomMergeStrategy;
import com.wcs.ai.alibaba.utils.Constants;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Configuration
public class MultiAgentConfig {

    @Bean
    public ReactAgent writerAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("writer_agent")
                .model(chatModel)
                .description("专业写作Agent")
                .instruction("你是一个知名的作家，擅长写作和创作。请根据用户的提问进行回答：{input}。")
                .outputKey("article")
                .build();
    }

    @Bean
    public ReactAgent reviewerAgent(ChatModel chatModel) {
            return ReactAgent.builder()
                    .name("reviewer_agent")
                    .model(chatModel)
                    .description("专业评审Agent")
                    .instruction(
                        "你是一个知名的评论家，擅长对文章进行评论和修改。" +
                        "对于散文类文章，请确保文章中必须包含对于西湖风景的描述。待评论文章： {article}" +
                        "最终只返回修改后的文章，不要包含任何评论信息。")
                    .outputKey("reviewed_article")
                .build();
    }

    /**
     * 工具调用模式：顺序执行
     * - subAgents 字段
     * @param writerAgent
     * @param reviewerAgent
     * @return
     */
    @Bean
    public SequentialAgent blogAgent(ReactAgent writerAgent, ReactAgent reviewerAgent) {
        return SequentialAgent.builder()
                .name("blog_agent")
                .description("根据用户给定的主题写一篇文章，然后将文章交给评论员进行评论")
                .subAgents(List.of(writerAgent, reviewerAgent))
                .build();
    }

    @Bean
    public ParallelAgent parallelAgent(ChatModel chatModel) {
        // 创建多个专业化Agent
        ReactAgent proseWriterAgent = ReactAgent.builder()
                .name("prose_writer_agent")
                .model(chatModel)
                .description("专门写散文的AI助手")
                .instruction("你是一个知名的散文作家，擅长写优美的散文。" +
                        "用户会给你一个主题：{input}，你只需要创作一篇100字左右的散文。")
                .outputKey("prose_result")
                .build();

        ReactAgent poemWriterAgent = ReactAgent.builder()
                .name("poem_writer_agent")
                .model(chatModel)
                .description("专门写现代诗的AI助手")
                .instruction("你是一个知名的现代诗人，擅长写现代诗。" +
                        "用户会给你的主题是：{input}，你只需要创作一首现代诗。")
                .outputKey("poem_result")
                .build();

        ReactAgent summaryAgent = ReactAgent.builder()
                .name("summary_agent")
                .model(chatModel)
                .description("专门做内容总结的AI助手")
                .instruction("你是一个专业的内容分析师，擅长对主题进行总结和提炼。" +
                        "用户会给你一个主题：{input}，你只需要对这个主题进行简要总结。")
                .outputKey("summary_result")
                .build();

        // 创建并行Agent
        return ParallelAgent.builder()
                .name("parallel_creative_agent")
                .description("并行执行多个创作任务，包括写散文、写诗和做总结")
                .mergeOutputKey("merged_results")
                .subAgents(List.of(proseWriterAgent, poemWriterAgent, summaryAgent))
//                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .mergeStrategy(new CustomMergeStrategy())
                .build();
    }

    @Bean
    public LlmRoutingAgent routingAgent(ChatModel chatModel) {
        // 创建专业化的子Agent
        ReactAgent writerAgent = ReactAgent.builder()
                .name("writer_agent")
                .model(chatModel)
                .description("擅长创作各类文章，包括散文、诗歌等文学作品")
                .instruction("你是一个知名的作家，擅长写作和创作。请根据用户的提问进行回答。")
                .outputKey("writer_output")
                .build();

        ReactAgent reviewerAgent = ReactAgent.builder()
                .name("reviewer_agent")
                .model(chatModel)
                .description("擅长对文章进行评论、修改和润色")
                .instruction("你是一个知名的评论家，擅长对文章进行评论和修改。" +
                        "对于散文类文章，请确保文章中必须包含对于西湖风景的描述。")
                .outputKey("reviewer_output")
                .build();

        ReactAgent translatorAgent = ReactAgent.builder()
                .name("translator_agent")
                .model(chatModel)
                .description("擅长将文章翻译成各种语言")
                .instruction("你是一个专业的翻译家，能够准确地将文章翻译成目标语言。")
                .outputKey("translator_output")
                .build();

        // 创建路由Agent
        return LlmRoutingAgent.builder()
                .name("content_routing_agent")
                .description("根据用户需求智能路由到合适的专家Agent")
                .model(chatModel)
                .subAgents(List.of(writerAgent, reviewerAgent, translatorAgent))
                .systemPrompt(Constants.ROUTING_SYSTEM_PROMPT) // 可以通过systemPrompt来提供额外的路由指导
                .instruction(Constants.ROUTING_INSTRUCTION) // 可以通过instruction来提供额外的路由指导
                .build();
    }

    @Bean
    public SupervisorAgent supervisorAgent(ChatModel chatModel) {
        // 监督者的子Agent
        ReactAgent translatorAgent = ReactAgent.builder()
                .name("translator_agent")
                .model(chatModel)
                .description("擅长将文章翻译成各种语言")
                .instruction("你是一个专业的翻译家，能够准确地将文章翻译成目标语言。待翻译文章： {article_content}。")
                .outputKey("translator_output")
                .build();

        ReactAgent reviewerAgent = ReactAgent.builder()
                .name("reviewer_agent")
                .model(chatModel)
                .description("擅长对文章进行评审和修改")
                .instruction("你是一个知名的评论家，擅长对文章进行评论和修改。待评审文章： {article_content}。")
                .outputKey("reviewer_output")
                .build();

        ReactAgent mainAgent = ReactAgent.builder()
                .name("general-assistant")
                .description("内容管理监督者的主agent")
                .model(chatModel)
                .build();

        // 创建监督者Agent
        return SupervisorAgent.builder()
                .name("content_supervisor")
                .description("内容管理监督者，负责协调写作、翻译等任务")
                .model(chatModel)
                .systemPrompt(Constants.SUPERVISOR_SYSTEM_PROMPT_NEW)  // 居然deprecated了
                .instruction(Constants.SUPERVISOR_INSTRUCTION)
                .subAgents(List.of(translatorAgent, reviewerAgent))
                .mainAgent(mainAgent)
                .build();
    }

    @Bean
    public SequentialAgent sequentialAgent(ChatModel chatModel, SupervisorAgent supervisorAgent) {
        // 第一个Agent：写文章
        ReactAgent writerAgent = ReactAgent.builder()
                .name("article_writer")
                .model(chatModel)
                .description("专业写作Agent，负责创作文章")
                .instruction("你是一个知名的作家，擅长写作和创作。请根据用户的提问进行回答：{input}。")
                .outputKey("article_content")
                .build();
        // 创建SequentialAgent，SupervisorAgent作为子Agent
        return SequentialAgent.builder()
                .name("content_processing_workflow")
                .description("内容处理工作流：先写文章，然后根据文章内容决定翻译或评审")
                .subAgents(List.of(writerAgent, supervisorAgent))
                .build();
    }

    /**
     * 该条件agent，官方文档的demo暂不可用
     */
//    @Bean
    public ConditionalAgent conditionalAgent(ChatModel chatModel) {
        // 创建两个分支Agent
        ReactAgent urgentAgent = ReactAgent.builder()
                .name("urgent_handler")
                .model(chatModel)
                .description("处理紧急请求")
                .instruction("你需要快速响应紧急情况...")
                .outputKey("urgent_result")
                .build();

        ReactAgent normalAgent = ReactAgent.builder()
                .name("normal_handler")
                .model(chatModel)
                .description("处理常规请求")
                .instruction("你可以详细分析和处理常规请求...")
                .outputKey("normal_result")
                .build();

        // 定义条件：检查输入是否包含"紧急"关键字
        Predicate<Map<String, Object>> isUrgent = state -> {
            Object input = state.get("input");
            if (input instanceof String) {
                return ((String) input).contains("紧急") || ((String) input).contains("urgent");
            }
            return false;
        };

        return ConditionalAgent.builder()
                .name("conditional_agent")
                .description("根据紧急程度路由请求")
                .condition(isUrgent)
                .trueAgent(urgentAgent)
                .falseAgent(normalAgent)
                .build();
    }

    @Bean
    public SequentialAgent complexAgent(ChatModel chatModel) {
        // 1. 创建研究Agent（并行执行）
        ReactAgent webResearchAgent = ReactAgent.builder()
                .name("web_research")
                .model(chatModel)
                .description("从互联网搜索信息的AI助手")
                .instruction("请搜索并收集关于以下主题的信息：{input}")
                .outputKey("web_data")
                .build();

        ReactAgent selfCreatorAgent = ReactAgent.builder()
                .name("self_creator")
                .model(chatModel)
                .description("自行创作营销文案的AI助手")
                .instruction("你是一个知名的自媒体营销号，擅长编写抓眼球的文案。" +
                        "用户会给你一个主题：{input}，你只需要创作一篇100字左右的营销文案。")
                .outputKey("create_data")
                .build();

        ParallelAgent researchAgent = ParallelAgent.builder()
                .name("parallel_research")
                .description("并行收集多个数据源的信息")
                .mergeOutputKey("research_data")
                .subAgents(List.of(webResearchAgent, selfCreatorAgent))
                .mergeStrategy(new CustomMergeStrategy())
                .build();

        /**
         *  当research_data类型是map时，此处好像没有办法读取
         *  当然也可以通过自定义MergeStrategy，让researchAgent的输出research_data变成一个字符串
          */
        // 2. 创建分析Agent
        ReactAgent analysisAgent = ReactAgent.builder()
                .name("analysis_agent")
                .model(chatModel)
                .description("分析研究数据")
                .instruction("请分析以下收集到的数据并提供见解：{research_data.web_data} 和 {research_data.create_data}")
                .outputKey("analysis_result")
                .build();

        // 3. 创建报告Agent（路由选择格式）
        ReactAgent pdfReportAgent = ReactAgent.builder()
                .name("pdf_report")
                .model(chatModel)
                .description("生成PDF格式报告")
                .instruction("""
              请根据研究结果和分析结果生成一份PDF格式的报告。
              
              研究结果：{research_data}
              分析结果：{analysis_result}
              """)
                .outputKey("pdf_report")
                .build();

        ReactAgent htmlReportAgent = ReactAgent.builder()
                .name("html_report")
                .model(chatModel)
                .description("生成HTML格式报告")
                .instruction("""
              请根据研究结果和分析结果生成一份HTML格式的报告。
              
              研究结果：{research_data}
              分析结果：{analysis_result}
              """)
                .outputKey("html_report")
                .build();

        LlmRoutingAgent reportAgent = LlmRoutingAgent.builder()
                .name("report_router")
                .description("根据需求选择报告格式")
                .model(chatModel)
                .subAgents(List.of(pdfReportAgent, htmlReportAgent))
                .build();

        // 4. 组合成顺序工作流
        return SequentialAgent.builder()
                .name("research_workflow")
                .description("完整的研究工作流：并行收集 -> 分析 -> 路由生成报告")
                .subAgents(List.of(researchAgent, analysisAgent, reportAgent))
                .build();
    }

    @Bean
    public SequentialAgent newComplexAgent(ChatModel chatModel) {
        // 1. 创建研究Agent（并行执行）
        // 创建多个专业化Agent
        ReactAgent proseWriterAgent = ReactAgent.builder()
                .name("prose_writer_agent")
                .model(chatModel)
                .description("专门写散文的AI助手")
                .instruction("你是一个知名的散文作家，擅长写优美的散文。" +
                        "用户会给你一个主题：{input}，你只需要创作一篇100字左右的散文。")
                .outputKey("prose_result")
                .build();

        ReactAgent poemWriterAgent = ReactAgent.builder()
                .name("poem_writer_agent")
                .model(chatModel)
                .description("专门写现代诗的AI助手")
                .instruction("你是一个知名的现代诗人，擅长写现代诗。" +
                        "用户会给你的主题是：{input}，你只需要创作一首现代诗。")
                .outputKey("poem_result")
                .build();

        ReactAgent summaryAgent = ReactAgent.builder()
                .name("summary_agent")
                .model(chatModel)
                .description("专门做内容总结的AI助手")
                .instruction("你是一个专业的内容分析师，擅长对主题进行总结和提炼。" +
                        "用户会给你一个主题：{input}，你只需要对这个主题进行简要总结。")
                .outputKey("summary_result")
                .build();

        // 创建并行Agent
        ParallelAgent researchAgent = ParallelAgent.builder()
                .name("parallel_creative_agent")
                .description("并行执行多个创作任务，包括写散文、写诗和做总结")
                .mergeOutputKey("research_data")
                .subAgents(List.of(proseWriterAgent, poemWriterAgent, summaryAgent))
//                .mergeStrategy(new ParallelAgent.DefaultMergeStrategy())
                .mergeStrategy(new CustomMergeStrategy())
                .build();

        // 2. 创建分析Agent
        ReactAgent analysisAgent = ReactAgent.builder()
                .name("analysis_agent")
                .model(chatModel)
                .description("分析研究数据")
                .instruction("请分析以下收集到的数据并提供见解：{research_data}")
                .outputKey("analysis_result")
                .build();

        // 3. 创建报告Agent（路由选择格式）
        ReactAgent pdfReportAgent = ReactAgent.builder()
                .name("pdf_report")
                .model(chatModel)
                .description("生成PDF格式报告")
                .instruction("""
              请根据研究结果和分析结果生成一份PDF格式的报告。
              
              研究结果：{research_data}
              分析结果：{analysis_result}
              """)
                .outputKey("pdf_report")
                .build();

        ReactAgent htmlReportAgent = ReactAgent.builder()
                .name("html_report")
                .model(chatModel)
                .description("生成HTML格式报告")
                .instruction("""
              请根据研究结果和分析结果生成一份HTML格式的报告。
              
              研究结果：{research_data}
              分析结果：{analysis_result}
              """)
                .outputKey("html_report")
                .build();

        LlmRoutingAgent reportAgent = LlmRoutingAgent.builder()
                .name("report_router")
                .description("根据需求选择报告格式")
                .model(chatModel)
                .subAgents(List.of(pdfReportAgent, htmlReportAgent))
                .build();

        // 4. 组合成顺序工作流
        return SequentialAgent.builder()
                .name("research_workflow")
                .description("完整的研究工作流：并行收集 -> 分析 -> 路由生成报告")
                .subAgents(List.of(researchAgent, analysisAgent, reportAgent))
                .build();
    }

}
