package com.wcs.ai.alibaba;

import com.wcs.ai.alibaba.service.AgentEngine;
import com.wcs.ai.alibaba.service.ChatModelEngine;
import com.wcs.ai.alibaba.utils.ApplicationContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiAlibabaStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAlibabaStudyApplication.class, args);
//		ApplicationContextUtils.getBean(AgentEngine.class).callAgent();
		ApplicationContextUtils.getBean(ChatModelEngine.class).callWithTool();
	}

}
