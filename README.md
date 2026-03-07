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
#### 最佳实践
- 始终使用检查点: 人工介入需要检查点机制来保存和恢复状态
- 提供清晰的描述: 在 ToolConfig 中提供清晰的描述，帮助审查者理解操作
- 保守编辑: 编辑工具参数时，尽量保持最小更改
- 处理所有工具反馈: 确保为每个需要审查的工具调用提供决策
- 使用相同的 threadId: 恢复执行时必须使用相同的线程 ID
- 考虑超时: 实现超时机制以处理长时间未响应的人工审批
### 记忆管理
- 长期记忆和短期记忆
  - 使用Store管理长期记忆，持久化记忆
  - 可以通过ModelHook或者Tool来处理长期记忆
  - 短期记忆：MemorySaver即可
### 多智能体(多agent)
- 工具调用 和 交接 两个模式
- 想象一下字节的coze，每个节点是一个agent，通过工作流把每个agent给关联起来
  - 每个agent都可以通过占位符读取state里的数据，这样前一个agent的执行结果就可以让后一个agent读取到
#### 结果合并
  - 顺序执行，串行结果
  - 并行执行，可以通过ParallelAgent.MergeStrategy来合并，支持自定义
  - 路由agent
    - 由大模型来决定调用哪个agent，但是这样没法在输入里传递参数，因为agent之间不知道顺序了，所以没法使用某个特性agent的输出结果了
  - 监督者：SupervisorAgent
    - 在监督者模式中，使用大语言模型（LLM）作为监督者，动态决定将任务路由到哪个子Agent，并支持多步骤循环路由。
    - 与 LlmRoutingAgent 不同，SupervisorAgent 支持子Agent执行完成后返回监督者，监督者可以根据执行结果继续路由到其他Agent或完成任务。
    - 流程
      - 监督者Agent接收用户输入或前序Agent的输出 
      - LLM分析当前状态并决定最合适的子Agent
      - 选中的子Agent处理任务
      - 子Agent执行完成后返回监督者
      - 监督者根据结果决定：
        - 继续路由到另一个子Agent（多步骤任务）
        - 返回 FINISH 完成任务
#### 自定义智能体
- ConditionalAgent
  - 文档太旧，代码不可参考
#### 复杂循环agent
- 感觉像是面向对象+面向过程编程
  - 抽象出主体流程节点，每个节点设置一个agent，归SequentialAgent管理
  - 每个子agent可以设置自己的agent
    - 子agent根据自己的需要，看是直接用原生agent，还是又是一个FlowAgent，管理其他子agent
### 智能体作为工具
- agent tool比较简单
  - 在yAgent的tools属性传入AgentTool.getFunctionToolCallback(xAgent)
### 工作流
### RAG
- 可以使用MessagesModelHook，ModelInterceptor，AgentHook 实现
  - 最明显的差别是AgentHook的实现，因为是agent的hook，所以只会在agent开始时，检索一次而已
- 所有方式都能实现两步 RAG：检索文档 → 增强上下文 → 生成答案。
- 最佳实践
  - 选择合适的架构：
    - 简单 FAQ → 两步 RAG
    - 复杂研究任务 → Agentic RAG
    - 需要质量保证 → 混合 RAG
