package com.wcs.ai.alibaba.service.ollama;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaEngine {

    @Resource
    private ChatModel ollamaChatModel;

    @SneakyThrows
    public void call4Test() {
        String result = ollamaChatModel.call("你是谁");
        System.out.println(result);
    }

}
