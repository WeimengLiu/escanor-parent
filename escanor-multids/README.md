# escanor-multids

该项目基于Spring-boot和Spring-cloud，主要实现多数据源配置以及使用docker部署

1. 多数据源支持

   通过实现`AbstractRoutingDataSource`类来切换数据源，可以根据线程变量来实现不同的`key`路由到不同的`dataSource`，项目基于`mysql`数据库

2. docker部署

   项目打包集成了docker打包插件，执行`clean package docker:build -DskipTests`即可将项目打包为镜像，基础镜像依赖于`java8`，打包好后可以使用以下命令启动

   ```shell
   docker run -p 8889:8889 -e "SPRING_PROFILES_ACTIVE=docker" -e "spring.cloud.consul.discovery.ip-address=10.162.16.34" -v $HOME/workspace/docker/log:/escanor-multids/logs --name escanor-multids -d   -t escanor/escanor-multids
   ```

   做了日志文件映射，避免重启导致丢失。

3. consul注册

   项目使用consul作为注册中心，同时为了解决开发和部署环境差异，利用`spring.profiles`做了配置文件分离。
