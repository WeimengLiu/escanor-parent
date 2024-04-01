#基础镜像，如果本地没有，会从远程仓库拉取。
FROM java:8

#镜像的制作人
MAINTAINER acceleratorwayne/acceleratorwayne@outlook.com

#工作目录
WORKDIR /escanor-job/

RUN chmod -R 755 /escanor-job

##这里的 /tmp 目录就会在运行时自动挂载为匿名卷，任何向 /tmp 中写入的信息都不会记录进容器存储层，可以多个
#VOLUME ["/tmp"]

#声明了容器应该打开的端口并没有实际上将它打开
#EXPOSE 8080
#设置时区
#ENV TZ=Asia/Shanghai

# 传递 SpringBoot 启动参数 和 JVM参数
ENV PARAMS="" JVM_OPTIONS=""
#定义参数
ARG JAR_FILE=escanor-job-1.0-SNAPSHOT.jar

# 将jar包添加到容器中并更名为app.jar
ADD target/${JAR_FILE} /escanor-job/app.jar

#拷贝本地文件到镜像中#复制上下文目录下的target/demo-1.0.0.jar 到容器里
#COPY mutil-datasource-0.0.1-SNAPSHOT.jar /mutil-datasource/app.jar

#设置umask
#RUN umask 0002

#指定容器启动时要执行的命令，但如果存在CMD指令，CMD中的参数会被附加到ENTRYPOINT指令的后面
ENTRYPOINT ["sh","-c", "java $JVM_OPTIONS -Djava.security.egd=file:/dev/./urandom -jar app.jar $PARAMS"]
