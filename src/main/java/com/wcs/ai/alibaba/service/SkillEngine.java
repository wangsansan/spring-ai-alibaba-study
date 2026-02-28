package com.wcs.ai.alibaba.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

@Service
public class SkillEngine {

    public static final String DESC_FORMAT = "请基于要求:%s，创建一个Skill";

    @Resource
    private ReactAgent skillAgent;

    @SneakyThrows
    public void createSkill(String desc) {
        AssistantMessage assistantMessage = skillAgent.call(String.format(DESC_FORMAT, desc));
        System.out.println(JSON.toJSONString(assistantMessage));
    }

}
