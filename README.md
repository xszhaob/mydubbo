模块说明
* dubbo-common 公共逻辑模块：包括Util类和通用模型。
* dubbo-remoting 远程通讯模块：相当于Dubbo协议的实现，如果RPC用RMI协议则不需要使用此包。
* dubbo-rpc 远程调用模块：抽象各种协议，以及动态代理，只含一对一的调用，不关心集群的管理。
* dubbo-cluster 集群模块：将多个服务提供方伪装为一个提供方，包括：负载均衡，容错，路由等，集群的地址列表可以是静态配置的，也可以是由注册中心下发。
* dubbo-registry 注册中心模块：基于注册中心下发地址的集群方式，以及对各种注册中心的抽象。
* dubbo-monitor 监控模块：统计服务调用次数、调用时间、调用链跟踪的服务。
* dubbo-config 配置模块：是Dubbo对外的API，用户通过Config使用Dubbo，隐藏Dubbo所有细节。
* dubbo-container 容器模块：是一个Standlone的容器，以简单的Main加载Spring启动，因为服务通常不需要Tomcat/JBoss等Web容器的特性，没必要用Web容器去加载服务。
