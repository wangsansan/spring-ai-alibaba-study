package com.wcs.ai.alibaba;

import com.wcs.ai.alibaba.rag.RagEngine;
import com.wcs.ai.alibaba.service.*;
import com.wcs.ai.alibaba.utils.ApplicationContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiAlibabaStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAlibabaStudyApplication.class, args);
//		ApplicationContextUtils.getBean(AgentEngine.class).callAgent();
//		ApplicationContextUtils.getBean(ChatModelEngine.class).callWithTool();
//		ApplicationContextUtils.getBean(ChatModelEngine.class).callTime();
//		ApplicationContextUtils.getBean(SkillEngine.class).createSkill("根据用户的描述的男生基本情况，设置三套属于他的风格的穿搭");
//		ApplicationContextUtils.getBean(AgentEngine.class).createPoemWithApproval();
//		ApplicationContextUtils.getBean(GraphEngine.class).runWorkflow();
//		ApplicationContextUtils.getBean(AdvancedAgentEngine.class).saveDataToStore();
//		ApplicationContextUtils.getBean(AdvancedAgentEngine.class).callWithModelStore();
//		ApplicationContextUtils.getBean(AdvancedAgentEngine.class).callWithCombinedMemory();
//		ApplicationContextUtils.getBean(AdvancedAgentEngine.class).callCrossSession();
//		ApplicationContextUtils.getBean(AdvancedAgentEngine.class).callWithPreferenceLearning();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).writeBlog();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).createByParallelAgent();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).createByRoutingAgent();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).createByComplexAgent();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).askConditionalAgent();
//		ApplicationContextUtils.getBean(MultiAgentEngine.class).createDataByComplexAgent();
//		ApplicationContextUtils.getBean(RagEngine.class).call();
//		ApplicationContextUtils.getBean(RagEngine.class).agenticCall();
//		ApplicationContextUtils.getBean(RagEngine.class).callWithMultiSourceAgent();
		ApplicationContextUtils.getBean(RagEngine.class).hybridCall();

	}

}
