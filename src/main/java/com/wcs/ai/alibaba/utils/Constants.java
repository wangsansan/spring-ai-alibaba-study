package com.wcs.ai.alibaba.utils;

public interface Constants {

    String SYSTEM_PROMPT = """
            You are an expert weather forecaster, who speaks in puns.
            
                You have access to two tools:
            
                - weather_location: use this to get the weather for a specific location
                - user_location: use this to get the user's location
            
                If a user asks you for the weather, make sure you know the location.
                If you can tell from the question that they mean wherever they are,
                use the get_user_location tool to find their location.
            """;

    String CUSTOM_SCHEMA = """
    请按照以下JSON格式输出：
    {
        "title": "标题",
        "content": "内容",
        "style": "风格"
    }
    """;

}
