# 使用官方的 OpenJDK 17 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制 Maven 配置文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 安装 Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# 构建应用
RUN mvn clean package -DskipTests

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# 启动应用
CMD ["java", "-jar", "/app/target/fresh-delivery-backend-1.0.0.jar"]