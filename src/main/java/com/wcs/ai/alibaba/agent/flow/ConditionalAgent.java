package com.wcs.ai.alibaba.agent.flow;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.FlowAgent;
import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowAgentBuilder;
import com.alibaba.cloud.ai.graph.agent.flow.builder.FlowGraphBuilder;
import com.alibaba.cloud.ai.graph.agent.flow.enums.FlowAgentEnum;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ConditionalAgent extends FlowAgent {

    private final Predicate<Map<String, Object>> condition;
    private final Agent trueAgent;
    private final Agent falseAgent;

    protected ConditionalAgent(ConditionalAgentBuilder builder) {
        super(builder.name, builder.description, builder.compileConfig,
                List.of(builder.trueAgent, builder.falseAgent));
        this.condition = builder.condition;
        this.trueAgent = builder.trueAgent;
        this.falseAgent = builder.falseAgent;
    }

    @Override
    protected StateGraph buildSpecificGraph(FlowGraphBuilder.FlowGraphConfig config)
            throws GraphStateException {
        // 使用顺序执行图结构，避免条件流的复杂配置
        return FlowGraphBuilder.buildGraph(
                FlowAgentEnum.SEQUENTIAL.getType(),
                config
        );
    }

    @SneakyThrows
    @Override
    public Optional<OverAllState> doInvoke(Map<String, Object> input, RunnableConfig runnableConfig)  {
        // 在invoke方法中实现条件逻辑
        if (condition.test(input)) {
            return trueAgent.invoke(input);
        } else {
            return falseAgent.invoke(input);
        }
    }

    public static ConditionalAgentBuilder builder() {
        return new ConditionalAgentBuilder();
    }

    /**
     * Builder for ConditionalAgent
     */
    public static class ConditionalAgentBuilder
            extends FlowAgentBuilder<ConditionalAgent, ConditionalAgentBuilder> {

        private Predicate<Map<String, Object>> condition;
        private Agent trueAgent;
        private Agent falseAgent;

        public ConditionalAgentBuilder condition(Predicate<Map<String, Object>> condition) {
            this.condition = condition;
            return this;
        }

        public ConditionalAgentBuilder trueAgent(Agent trueAgent) {
            this.trueAgent = trueAgent;
            return this;
        }

        public ConditionalAgentBuilder falseAgent(Agent falseAgent) {
            this.falseAgent = falseAgent;
            return this;
        }

        @Override
        public ConditionalAgent doBuild() {
            validate();
            if (condition == null || trueAgent == null || falseAgent == null) {
                throw new IllegalStateException(
                        "Condition, trueAgent and falseAgent must be set");
            }
            return new ConditionalAgent(this);
        }

        @Override
        protected ConditionalAgentBuilder self() {
            return this;
        }
    }
}