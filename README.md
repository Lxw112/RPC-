![image](https://github.com/user-attachments/assets/21660df9-4eca-4edc-a765-1f44691a511c)
#一、静态代理实现
​ 静态代理是指为每一个特定类型的接口或者对象提供一个代理类进行代理操作，此处创建UserServiceProxy，实现代理方法。

​ 此处代理方法的实现，不是直接复制那段UserServicelmpl相关的代码，而是通过Http请求去调用接口服务（此处需注意参数的序列化处理）

构建步骤说明：

【1】pom.xml中引入noob-rpc-easy依赖（需用到序列化器）

【2】自定义UserServiceProxy代理类实现代理

【3】修改调用方法：将UserService实现交由UserServiceProxy代理处理
总结：静态代理其实就是变相实现UserService接口，只不过其代理操作核心是通过http调用对应的服务接口。但是基于这种方式，如果后续有多个接口需要调用，则需要相应编写不同的代理类，这种代理方式的灵活性会非常差，因此引入动态代理进行构建
#二、动态代理实现
​ 动态代理的作用：根据要生成的对象的类型，自动生成一个代理对象。此处使用JDK动态代理实现
​ 常用的动态代理实现方式有JDK动态代理和基于字节码生成的动态代理(比如CGLIB)。前者简单易用、无需引入额外的库，但缺点是只能对接口进行代理；后者更灵活、可以对任何类进行代理，但性能略低于JDK动态代理。
构建步骤说明：
【1】在noob-easy-rpc模块中编写动态代理类ServiceProxy（实现InvocationHandler的invoke方法，此处对比静态代理的实现内容基本大同小异）
【2】使用工厂设计模式，简化对象创建过程
【3】在sample-consumer中EasyConsumerSample修改代理模式
#三、全局配置加载
维护全局配置对象
​ RPC框架中需要维护一个全局的配置对象。在引入RPC框架的项目启动时，从配置文件中读取配置并创建对象实例，之后就可以集中地从这个对象中获取配置信息，而不用每次加载配置时再重新读取配置、并创建新的对象，减少了性能开销。
​ 使用设计模式中的单例模式，就能够很轻松地实现这个需求了。一般情况下，使用holder来维护全局配置对象实例。项目中，可以使用RpcApplication类作为RPC项目的启动入口、并且维护项目全局用到的变量。
RpcApplication
​ 双检锁单例模式实现：支持在获取配置时才调用init方法实现懒加载。可支持传入配置对象，如果不传入默认引入ConfigUtils来加载配置，基于这种设计只需要一行代码即可加载配置
RpcConfig rpc = RpcApplication.getRpcConfig();
---实现了支持读取application.yaml格式的配置文件
#四、接口Mock实现
1】Mock概念引入：类似模拟数据（针对一些开发场景下无法调通服务的情况，提供一些模拟服务响应数据模拟远程服务行为，便于开发流程调通）
【2】Mock实现：设定mock属性（确认框架是否开启mock模式），通过代理模式构建MockService
RPC框架的核心功能是调用其他远程服务。但是在实际开发和测试过程中，有时可能无法直接访问真实的远程服务，或者访问真实的远程服务可能会产生不可控的影响，例如网络延迟、服务不稳定等。在这种情况下，就需要使用mock服务来模拟远程服务的行为，以便进行接口的测试、开发和调试。
​ mock是指模拟对象，通常用于测试代码中，特别是在单元测试中，便于跑通业务流程。
通过mock概念给orderService设定一个模拟对象，调用order方法时随便返回一个值，以便于后续流程执行。虽然mock服务并不是RPC框架的核心能力，但是它的开发成本并不高。而且给RPC框架支持mock后，开发者就可以轻松调用服务接口、跑通业务流程，不必依赖真实的远程服务，提高使用体验，何乐而不为。可以通过最简单的方式（例如一个配置），就让开发者使用mock服务
​ 动态代理可以实现动态创建对象，因此此处可以通过动态代理创建一个调用方法时返回固定值的对象来实现模拟。
实现步骤
构建步骤说明
【1】RpcConfig设定mock字段（是否开启mock）
【2】在proxy包下创建MockServiceProxy类，生成mock代理服务
【3】在proxy包下新增MockServiceProxyFactory代理服务工厂，提供获取mock代理对象的方法getMockProxy
​ （此处针对mock的动态代理实现，为了避免可service服务相关的代理实现混淆，此处单独将mock抽出来一套）
【4】修改proxy下ServiceProxyFactory代理逻辑，补充验证mock参数以确认是否调用mock服务
#五、序列化器与SPI机制
SPI机制：SPI (Service Provider Interface)服务提供接口是Java的机制，主要用于实现模块化开发和插件化扩展。
​SPI机制允许服务提供者通过特定的配置文件将自己的实现注册到系统中，然后系统通过反射机制动态加载这些实现，而不需要修改原始框架的代码，从而实现了系统的解耦、提高了可扩展性。
SPI的实现分为系统实现和自定义实现。
系统实现
​ Java内已经提供了SPI机制相关的API接口，可以直接使用，这种方式最简单。
【1】在resources 资源目录下创建META-INF/services 目录，创建一个名称为要实现的接口的空文件
【2】文件中填写自己定制的接口实现类的完整路径
​ 此处需注意文件存放路径、文件名称、内容中的类路径定义（如果配置关联不到则会提示No service provider "xxxxx" found）
【3】使用系统内置的ServiceLoader动态加载指定接口的实现类
自定义实现
​ 系统实现SPI虽然简单，但是如果想定制多个不同的接口实现类，就没办法在框架中指定使用哪一个了， 也就无法实现“通过配置快速指定序列化器”的需求。
​ 因此需要自己定义SPI机制的实现，只要能够根据配置加载到类即可。比如读取如下配置文件，能够得到一个序列化器名称=>序列化器实现类对象的映射，之后就可以根据用户配置的序列 化器名称动态加载指定实现类对象
jdk=com.noob.rpc.serializer.JdkSerializer
hessian=com.noob.rpc.serializer.HessianSerializer
json=com.noob.rpc.serializer.JsonSerializer
kryo=com.noob.rpc.serializer.KryoSerializer
#六、注册中心实现和优化
【1】服务提供方根据配置将服务注册到注册中心
【2】服务调用方根据配置查询注册中心对应服务的注册信息，随后根据获取到的请求地址进行服务调用
构建步骤说明
【1】注册中心开发：注册信息定义（ServiceMetaInfo）
注册中心配置
​ 在config包下编写注册中心配置类RegistryConfig， 让用户配置连接注册中心所需的信息，比如注册中心类别、注册中心地址、户名、密码、连接超时时间等。
注册中心接口和实现
注册中心接口：registry/Registry
注册中心接口实现：registry/EtcdRegistry
支持配置和扩展注册中心（参考SPI机制扩展实现）
（1）引入RegistryKeys（列举所有支持注册中心键名）
（2）引入RegistryFactory，支持根据key从SPI获取注册中心对象实例（参考序列化器工厂实现SerializerFactory）
（3）SPI配置文件装载（在META_INF/rpc/system目录下编写注册中心接口的SPI配置文件）
（4）初始化注册中心
​ 参考序列化器注册，在对应的位置进行初始化。此处考虑服务消费者和服务提供者都需要在注册中心建立连接（RPC框架中必不可少的环节），因此此处可以将初始化流程放在RpcApplication中进行操作
【2】注册中心优化
心跳检测和续期机制
心跳检测概念
​心跳检测(俗称heartBeat)：是一种用于监测系统是否正常工作的机制。它通过定期发送心跳号(请求)来检目标系统的状态。
​如果接收方在一定时间内没有收到心跳信号或者未能正常响应请求，就会认为目标系统故障或不可用，从而触发相应的处理或告警机制。
从心跳检测的概念来看，实现心跳检测一般需要2个关键属性：定时、网络请求。
​ 使用Etcd实现心跳检测会简单一些，因为Etcd自带了key过期机制，可以试着换个思路：给节点注册信息一个“生命倒计时”，让节点定期续期，重置己的倒计时。如果节点已宕机，一直不续期，Etcd 就会对key进行过期删除。即如果到时间还不续期则为系统宕机了。
​ 在Etcd中，实现心跳检测和续期机制，可以遵循如下步骤：
【1】服务提供者向Etcd注册自己的服务信息，并在注册时设置TTL (生存时间)
【2】Etcd在接收到服务提供者的注册信息后，会自动维护服务信息的TTL，并在TTL过期时删除该服务信息。
【3】服务提供者定期请求Etcd续签自己的注册信息，写TTL
​ 需要注意的是，续期时间一定要小于过期时间，允许一次容错的机会。
实现步骤
构建步骤说明
【1】给Registry补充心跳检测方法heartBeat定义，并在EtcdRegistry中实现方法
【2】维护续期节点集合
【3】开启heartBeat
服务节点下线机制
服务节点下线机制概念
​ 当服务提供者节点宕机时，应该从注册中心移除掉已注册的节点，否则会影响消费端调用，需要设计一套服务节点下线机制。
服务节点下线又分为：
主动下线：服务提供者项目正常退出时，主动从注册中心移除注册信息
被动下线：服务提供者项目异常推出时，利用Etcd的key过期机制自动移除
​ 被动下线已经可以利用Etcd的机制实现，因此主要开发主动下线。此处思考一个问题：怎么在Java项目正常退出时，执行某个操作呢? 利用JVM的ShutdownHook就能实现
​ JVM的ShutdownHook是Java虚拟机提供的一种机制，允许开发者在JVM即将关闭之前执行一些清理工作或其他必要的操作，例如关闭数据库连接、释放资源、保存临时数据等。Spring Boot也提供了类似的优雅停机能力。
实现步骤
构建步骤说明
【1】完善Etcd注册中心的destory方法，补充下线节点的逻辑
【2】RpcApplication的init方法中注册Shutdown Hook（当程序正常退出的时候会执行注册中心的destroy方法）
消费端服务缓存
​ 正常情况下，服务节点信息列表的更新频率是不高的，所以在服务消费者从注册中心获取到服务节点信息列表后，可以缓存在本地，下次就不用再请求注册中心获取了，能够提高性能。
实现步骤
构建步骤说明
【1】增加本地缓存：registry/RegistryServiceCache
【2】使用本地缓存：修改EtcdRegistry处理逻辑，使用本地缓存对象
【3】服务缓存更新：监听机制（当服务注册信息发生变更时，需要更新消费端服务缓存
服务缓存更新（服务端服务信息更新，同步更新到缓存）
当服务注册信息发生变更(比如节点下线)时，需要即时更新消费端缓存。
思考一个问题：如何知道服务注册信息什么时候发生变更呢?
需要使用Etcd的watch监听机制，当监听的某个key发生修改或删除时，就会触发事件来通知监听者。
image-20240414124819324
首先要明确watch监听是服务消费者还是服务提供者执行的。目标是更新缓存，缓存是在服务消费端维护和使用的，所以也应该是服务消费端去watch
也就是说，只有服务消费者执行的方法中，可以创建watch监听器，那么比较合适的位置就是服务发现方法(serviceDiscovery)。可以对本次获取到的所有服务节点key进行监听
还需要防止重复监听同一个key,可以通过定义一个已监听key的集台来实现。
构建步骤说明
【1】在Registry新增watch监听方法定义
【2】在Etcd中实现监听方法，定义监听key集合（维护监听列表）并实现监听逻辑
【3】EtcdRegistry中修改服务发现逻辑（添加监听：调用watch方法）
#七、自定义协议实现
实现步骤
构建步骤说明
【1】新建protocol包，存放所有和自定义协议相关的代码（协议消息类ProtocolMessage、协议常量类ProtocolConstant、消息字段枚举类ProtocolMessageStatusEnum、消息类型枚举ProtocolMessageTypeEnum、序列化器枚举ProtocolMessageSerializerEnum）
【2】网络传输相关：server.tcp包
ProtocolMessage
​ProtocolMessage：将消息头单独封装为一个内部类，消息体可以使用泛型类型
ProtocolConstant
​ProtocolConstant：记录了和自定义协议有关的关键信息，例如消息头、魔数、版本号信息等
ProtocolMessageStatusEnum
​ProtocolMessageStatusEnum：消息字段枚举类相关：协议状态枚举（暂定成功、请求失败、响应失败）
ProtocolMessageTypeEnum
​ProtocolMessageTypeEnum：协议消息类型枚举（请求、响应、心跳、其他等）
ProtocolMessageSerializerEnum
​ProtocolMessageSerializerEnum：序列化器枚举（和RPC框架支持的序列化器对应）
在server.tcp包下重新写NettyTcpServer和NettyTcpClient类，将之前的服务提供者（Netty）-----服务消费者（发送http请求）改为服务提供者（Netty）-------服务消费者（Netty）
【3】.编码器和解码器
​ 基于上面的实现中，Netty的TCP服务器收发的消息是ByteBuf类型，不能直接写入一个对象。因此，需要自定义一个编码器和解码器，将Java的消息对象和ByteBuf进行相互转换。
​ http请求响应处理：从body处理器中获取到body字节数组，再通过序列化（反序列化）得到RpcRequest/RpcResponse对象。使用TCP服务器后调整为从ByteBuf中获取字节数组，然后再转码为RpcRequest/RpcResponse对象（相关处理流程都是可以复用的）
【4】添加请求处理器和响应处理器
#八、负载均衡（服务消费端）
1.多种负载均衡器的实现
负载均衡器通用接口
​编写负载均衡器通用接口：提供一个选择服务方法，接受请求参数和可用服务列表，可以根据这些信息进行选择
轮询负载均衡器
​使用JUC包下的AtomicInteger实现院子计数器，防止并发冲突问题
随机负载均衡器
​使用Java自带的Random类实现随机选择
一致性 Hash负载均衡器
​使用TreeMap实现一致性Hash环，该数据结构提供了ceilingEntry 和firstEntry两个方法，便于获取符合算法要求的节点。
​此处实现注意两个点：
（1）根据requestParams对象计算Hash值，此处只是简单地调用了对象的hashCode方法,可以根据需求实现自己的Hash算法
（2）每次调用负载均衡器时，都会重新构造Hash环,这是为了能够即时处理节点的变化
2.支持配置和扩展负载均衡器
​ 一个成熟的RPC框架可能会支持多个负载均衡器，像序列化器和注册中心-样，目前设定需求是让开发者能够填写配置来指定使用的负载均衡器，并且支持自定义负载均衡器，让框架更易用、更利于扩展。要实现这点，开发方式和序列化器、注册中心都是一样的，都可以使用工厂创建对象、使用SPI动态加载自定义的注册中心。
（1）定义LoadBalancerKeys负载均衡器常量：定义所有支持的负载均衡器键名
（2）工厂模式构建：LoadBalancerFactory（SpiLoader装配）
​ LoadBalancerFactory可参考此前的序列化器、注册中心相关进行修改即可
（3）SPI机制装配：在META-INF的rpc/system目录下引入负载均衡接口的SPI配置文件
（4）为RpcConfig全局配置新增负载默认负载均衡器配置
3.应用负载均衡器（服务消费方）
​修改ServiceProxy代码，将原有“固定调用第一个服务节点”调整为“调用负载均衡器获取一个服务节点”
