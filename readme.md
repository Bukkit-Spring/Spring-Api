## Bukkit-Spring

Bukkit-Spring 将 Spring Boot 生态与 Bukkit 进行了兼容 (目前支持 1.12.2 版本)

### 目前已整合的三方框架
 - Redis
 - Redisson
 - Mybatis Plus (项目内提供为多数据源配置 如需动态切换需搭配Kotlin-Expansion)
 - PageHelper

### 如何使用?
Spring-API 为与 Spring 的兼容层, 您的所有插件需要在 plugin.yml 添加其为依赖, 便可正常使用 Spring 的任何功能。

Spring-API 的打包略微不同, 分为两种方式打包。

#### 作为开发API使用
1. 注释掉 maven 打包插件中的前两个, 只留最后一个(maven-assembly-plugin)
2. `mvn clean`
3. `mvn assembly:assembly`
4. 将打包出的 `Spring-1.0.0-RELEASE-jar-with-dependencies.jar` 作为开发依赖即可

#### 打包插件放入服务器使用
1. 注释掉 maven 打包插件中的最后一个, 只留前面两个
2. `mvn clean`
3. `mvn package`
4. 将打包出的 `Spring-1.0.0-RELEASE.jar` 与 `Spring-Api` 文件夹, 共同放入服务器插件目录下, 并启动服务器即可 (默认配置文件无任何配置信息 启动会报错)
