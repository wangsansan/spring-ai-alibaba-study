package com.wcs.ai.alibaba.messages;

import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;

import java.net.URL;
import java.util.Map;

public class MessageExample {

    public void buildMsgWithMeta() {
        UserMessage userMsg = UserMessage.builder()
                .text("你好！")
                .metadata(Map.of(
                        "user_id", "alice",  // 可选：识别不同用户
                        "session_id", "sess_123"  // 可选：会话标识符
                ))
                .build();
    }

    @SneakyThrows
    public void buildMsgWithMedia() {
        // 从 URL 创建图像
        UserMessage userMsg = UserMessage.builder()
                .text("描述这张图片的内容。")
                .media(Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_JPEG)
                        .data(new URL("https://example.com/image.jpg"))
                        .build())
                .build();
    }

}
