# 1. 使用 Maven 进行构建（构建阶段）
FROM maven:3.9.9-eclipse-temurin-17-focal AS build
WORKDIR /app
# 复制依赖文件并下载
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 2. 使用轻量级 JDK 运行（运行阶段）
FROM openjdk:17-jdk-alpine
WORKDIR /app
# 从构建阶段把打好的 jar 包拿过来
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]