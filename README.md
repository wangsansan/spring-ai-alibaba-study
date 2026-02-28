## 介绍
- this is a project created by wcs
- to record the process of study spring-ali-alibaba
- 学习文档：https://java2ai.com/docs/overview

## ChatClient 和 ReactAgent的区别
- ChatClient是spring-ai框架提供的
- ReactAgent是spring-ai-alibaba提供的
- ReactAgent自动记录chatModel的交互历史记录


### 对于ChatClient来说，tools 和 toolCallbacks 的区别
- 其实没啥区别，传参进去的tools，底层会封装成为toolCallbacks


### chatOptions
- 单次交互设置属性
- 如果想要多次交互时使用的tool或者Config，需要配置到ChatClient或者ReactAgent上

## 注释里面加上图片
https://java2ai.com/img/agent/agents/reactagent.png

## skills的作用
- 类似于执行tool的SOP，只不过这个SOP是基于业务流程写给大模型看的

