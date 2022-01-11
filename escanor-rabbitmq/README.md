# rabbitMq学习项目
1. 项目实现了消息监听动态配置，可以参考配置类`RabbitMqConfig`，配置文件如下：
```yaml
spring:
  rabbitmq:
    queue:
      listeners:
        testListener:
          queueNames: test
          minConsumer: 1
          maxConsumer: 1
          threadMaxSize: 1
      enableListener: true
```
2. 配置过程集成了sleuth的相关处理，带入`Trace`上线文, 通过如下代码创建`RabbitListenerContainerFactory`，随后使用获得的`factory`即可。

```java
@Autowired
private SpringRabbitTracing springRabbitTracing;

SimpleRabbitListenerContainerFactory factory=springRabbitTracing.newSimpleRabbitListenerContainerFactory(connectionFactory);
```

3. 增加`logback`滚动日志刷新等相关配置, 如下：
```xml
<!--滚动策略，按照时间滚动 TimeBasedRollingPolicy-->
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <!--文件路径,定义了日志的切分方式——把每一天的日志归档到一个文件中,以防止日志填满整个磁盘空间-->
    <FileNamePattern>${logPath}/%d{yyyy-MM}/${appName}.%d{yyyy-MM-dd}.log</FileNamePattern>
    <!--只保留最近30天的日志-->
    <maxHistory>30</maxHistory>
    <!--用来指定日志文件的上限大小，那么到了这个值，就会删除旧的日志-->
    <totalSizeCap>1GB</totalSizeCap>
</rollingPolicy>
```
4. TO-DO: 增加消息发送等相关封装类。