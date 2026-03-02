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

## 高级功能
### 上下文工程
- 使用 hook 和 interceptors 实现上下文工程
- 譬如通过ModelInterceptor，对prompt进行增强，
  - 譬如新增SystemMessage
  - 加载当前登录用户的的喜好（从store里面加载），选择可使用的工具，类似于数据权限的控制
  - 消息过滤（只保留最近 N 条历史消息）
  - 响应格式
- 工具上下文：创建工具，修改调用过程中的各种流程
  - 执行过程中读取或者修改状态等数据
- 生命周期上下文
  - 继承 ModelHook， HookPosition
    - beforeAgent
    - afterAgent
    - beforeModel
    - afterModel
  - 继承 MessagesModelHook
### 人工介入
- 中断决策类型
  - approve： 操作被原样批准并执行，不做任何更改	； example： 完全按照写好的内容发送电子邮件
  - edit: 工具调用将被修改后执行	            ; example: 在发送电子邮件之前更改收件人
  - reject :工具调用被拒绝，并向对话中添加解释   ; 拒绝电子邮件草稿并解释如何重写
#### 单独agent和workflow中的agent
- 注意事项
  - 共享检查点保存器: 工作流和嵌套的 Agent 应该使用相同的检查点保存器实例，以确保状态一致性
  - 线程 ID 一致性: 恢复执行时必须使用相同的 threadId
  - 空输入恢复: 恢复执行时通常传入空的 Map，因为状态已保存在检查点中
  - 节点标识: InterruptionMetadata.node() 返回的是工作流中 Agent 节点的名称，而不是 Agent 内部的节点名称