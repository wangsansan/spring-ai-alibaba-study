Kafka 介绍（可直接复制版）

# Kafka 简介

## 1. 什么是 Kafka

Apache Kafka 是一个**分布式、高吞吐、低延迟、可持久化**的**消息队列 / 事件流平台**，核心用于异步解耦、削峰填谷、日志收集、实时数据管道、流计算（对接 Flink、Spark 流、Kafka Streams）。

## 2. 核心特点

- 高吞吐：单机可处理几十万条消息/秒，适配高并发场景

- 高可用：多副本机制，支持集群部署，故障可自动转移，避免数据丢失

- 持久化：消息落地磁盘存储，支持重复消费，可追溯历史消息

- 水平扩展：集群节点可按需扩容，不影响现有服务运行

- Exactly-Once 语义：支持端到端一次性投递，避免消息重复或丢失

## 3. 核心概念

### 3.1 基础组件

- Broker：Kafka 服务节点，一个独立节点即为一个 Broker，多个 Broker 组成集群

- Topic：消息的逻辑分类，生产者向指定 Topic 发送消息，消费者从指定 Topic 消费消息

- Partition：分区，一个 Topic 可拆分多个 Partition，实现并行读写，提升吞吐量

- Offset：消息在分区内的唯一标识（偏移量），消费者通过记录 Offset 控制消费进度

### 3.2 生产与消费

- Producer：生产者，负责向 Kafka Topic 发送消息，可配置消息分区策略

- Consumer：消费者，负责从 Kafka Topic 拉取消息并处理

- Consumer Group：消费者组，同一组内的消费者分工消费 Topic 的不同分区，实现负载均衡；不同组可独立消费同一 Topic

### 3.3 可靠性保障

- Replication：副本数，每个 Partition 可设置多个副本，分为 Leader 副本（对外提供读写）和 Follower 副本（同步 Leader 数据）

- ISR（In-Sync Replicas）：同步中的副本集合，确保 Leader 故障时，可快速选举新 Leader

- ACK 机制（消息确认机制）：

    - acks=0：生产者发送消息后不等待确认，速度最快，可能丢失消息

    - acks=1：仅 Leader 副本写入消息后确认，性能与可靠性兼顾

    - acks=-1/all：所有 ISR 副本写入消息后才确认，可靠性最高

## 4. 典型应用场景

1. 微服务异步通信：如订单系统、支付系统、物流系统之间的解耦通信

2. 日志统一收集：搭配 ELK 栈（Elasticsearch + Logstash + Kibana），收集各服务日志

3. 用户行为埋点：收集 APP/网站用户点击、浏览等行为数据，用于分析和推荐

4. 实时数据处理：作为实时数仓、实时大屏、实时推荐系统的数据源

5. CDC 数据同步：通过 Canal 采集数据库变更，经 Kafka 同步至下游存储或计算系统

## 5. 常用架构

```

Producer → Kafka Cluster（Topic + Partition + Replica）→ Consumer Group → 业务系统/存储系统/流计算系统

```

## 6. 常用基础命令

```bash

# 1. 创建 Topic（指定分区数3，副本数1）

kafka-topics.sh --create --topic my_topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 2. 启动生产者，发送消息

kafka-console-producer.sh --topic my_topic --bootstrap-server localhost:9092

# 3. 启动消费者，从头消费消息

kafka-console-consumer.sh --topic my_topic --bootstrap-server localhost:9092 --from-beginning

# 4. 查看 Topic 详情

kafka-topics.sh --describe --topic my_topic --bootstrap-server localhost:9092

```
