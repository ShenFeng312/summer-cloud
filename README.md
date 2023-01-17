

# SUMMER-CLOUD
一个自娱自乐的项目 从头开始写一个框架 主要为了自己温习,同时也方便其他同学学习
# Build from Source
需要JDK11(后面会升级为JDK17 因为是自己玩的)  
该项目基于springboot2.7.2(后面会升级为3 因为是自己玩的)  
执行 `mvn clean install -DskipTests` 构建项目
## 功能列表
[配置中心](#配置中心)  
[远程调用](#远程调用)

# 配置中心
支持配置中心客户端服务端  
具体可参考demo/config-demo

## 客户端
1. 引入依赖 目前还没上传到maven 仓库 需要自己 install
```xml
		<dependency>
			<groupId>org.summer.cloud</groupId>
			<artifactId>spring-boot-starter-summer-cloud-config</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
```
2. 配置文件
```
spring:
  application:
    name: config_demo
  config:
    import: summer-cloud:${spring.application.name}.yaml #需要导入的配置文件，格式为summer-cloud:fileName
summer:
  cloud:
    config:
      url: http://localhost:1106  #配置中心服务器地址
```
## 配置中心启动
`org.summer.cloud.config.server.SummerCloudConfigServerApplication` 可以自己打包后启动
默认端口1106
目前支持3个接口
```java
//获取对应文件名的配置文件
    @GetMapping("/config/{fileName}")
    public ConfigEntity config(@PathVariable String fileName) {
        return configRepository.getConfig(fileName);
    }
    
//获取文件的版本号判断是否需要更新用
    @GetMapping("/version/{fileName}")
    public int version(@PathVariable String fileName) {
        return configRepository.getVersion(fileName);
    }
//上传配置文件
    @PostMapping("config/{fileName}")
    public String config(@PathVariable String fileName, @RequestBody String config) {
        configRepository.save(fileName, config);
        return "OK";
    }
```
目前配置文件不持久化到磁盘和数据库 如有需求请自己实现 代码很简单

# 远程调用
目前只实现了基于http的rpc 远程调用
依赖
```xml
		<dependency>
			<groupId>org.summer.cloud</groupId>
			<artifactId>spring-boot-starter-summer-cloud-rpc-http</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
```
具体可参考demo/rpc-demo
## provider端
```java
@SummerCloudService
public class DemoServiceImpl implements DemoService {
	@Override
	public String hello(String name) {
		return "hello,"+name;
	}
}
```
## consumer端
```java
	@SummerCloudResource(host = "http://localhost:9090")
	DemoService demoService;
```
