![image](https://github.com/user-attachments/assets/21660df9-4eca-4edc-a765-1f44691a511c)
# 一、静态代理实现
​ 静态代理是指为每一个特定类型的接口或者对象提供一个代理类进行代理操作，此处创建UserServiceProxy，实现代理方法。

​ 此处代理方法的实现，不是直接复制那段UserServicelmpl相关的代码，而是通过Http请求去调用接口服务（此处需注意参数的序列化处理）

构建步骤说明：

【1】pom.xml中引入noob-rpc-easy依赖（需用到序列化器）

【2】自定义UserServiceProxy代理类实现代理

【3】修改调用方法：将UserService实现交由UserServiceProxy代理处理
总结：静态代理其实就是变相实现UserService接口，只不过其代理操作核心是通过http调用对应的服务接口。但是基于这种方式，如果后续有多个接口需要调用，则需要相应编写不同的代理类，这种代理方式的灵活性会非常差，因此引入动态代理进行构建
# 二、动态代理实现
​ 动态代理的作用：根据要生成的对象的类型，自动生成一个代理对象。此处使用JDK动态代理实现
​ 常用的动态代理实现方式有JDK动态代理和基于字节码生成的动态代理(比如CGLIB)。前者简单易用、无需引入额外的库，但缺点是只能对接口进行代理；后者更灵活、可以对任何类进行代理，但性能略低于JDK动态代理。
构建步骤说明：
【1】在noob-easy-rpc模块中编写动态代理类ServiceProxy（实现InvocationHandler的invoke方法，此处对比静态代理的实现内容基本大同小异）
【2】使用工厂设计模式，简化对象创建过程
【3】在sample-consumer中EasyConsumerSample修改代理模式
# 三、全局配置加载
维护全局配置对象
​ RPC框架中需要维护一个全局的配置对象。在引入RPC框架的项目启动时，从配置文件中读取配置并创建对象实例，之后就可以集中地从这个对象中获取配置信息，而不用每次加载配置时再重新读取配置、并创建新的对象，减少了性能开销。
​ 使用设计模式中的单例模式，就能够很轻松地实现这个需求了。一般情况下，使用holder来维护全局配置对象实例。项目中，可以使用RpcApplication类作为RPC项目的启动入口、并且维护项目全局用到的变量。
RpcApplication
​ 双检锁单例模式实现：支持在获取配置时才调用init方法实现懒加载。可支持传入配置对象，如果不传入默认引入ConfigUtils来加载配置，基于这种设计只需要一行代码即可加载配置
RpcConfig rpc = RpcApplication.getRpcConfig();
---实现了支持读取application.yaml格式的配置文件
# 四、接口Mock实现
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
# 五、序列化器与SPI机制
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
# 六、注册中心实现和优化
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
# 七、自定义协议实现
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
# 八、负载均衡（服务消费端）
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
# 九、重试机制（服务消费端）
## 1.重试策略
fault.retry包存储重试机制相关内容：
【1】编写重试策略通用接口：RetryStrategy
【2】编写不同重试策略：不重试策略、固定重试间隔策略
【3】重试测试：RetryStrategyTest（单元测试，验证不同的重试策略）
【4】SPI+工厂实现支持配置和扩展重试策略，并结合项目引用
重试策略设计
noob-rpc-core:pom.xml
<!-- 引入重试库 https://github.com/rholder/guava-retrying -->
        <dependency>
            <groupId>com.github.rholder</groupId>
            <artifactId>guava-retrying</artifactId>
            <version>2.0.0</version>
        </dependency>
RetryStrategy：重试策略接口
/**
 * 重试策略
 */
public interface RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
NoRetryStrategy：不重试策略

/**
 * 不重试 - 重试策略
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy {

    /**
     * 重试
     * @param callable
     * @return
     * @throws Exception
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }

}
FixedIntervalRetryStrategy：固定时间间隔重试策略
/**
 * 固定时间间隔 - 重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
重试条件：使用retrylfExceptionOfType方法指定当出现Exception异常时重试
重试等待策略：使用withWaitStrategy方法指定策略，选择fixedWait固定时间间隔策略
重试停止策略：使用withStopStrategy方法指定策略,选择stopAfterAttempt超过最大重试次数停止
重试工作：使用withRetryListener监听重试，每次重试时，除了再次执行任务外，还能够打印当前的重试次数
RetryStrategyTest：重试策略测试

/**
 * 重试策略测试
 */
public class RetryStrategyTest {

    // 指定重试策略进行测试
//    RetryStrategy retryStrategy = new NoRetryStrategy();
    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    @Test
    public void doRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
![image](https://github.com/user-attachments/assets/1e88e2ba-3426-423d-a2cb-db2581e13e01)

## 2.支持配置和扩展重试策略
​参考序列化器、注册中心、负载均衡的配置和扩展实现，基于SPI机制和工厂模式进行构建
【1】RetryStrategyKeys：存储重试策略常量
【2】RetryStrategyFactory：重试策略工厂（SPI加载）
【3】定义SPI配置文件：在resource/META-INF/rpc/system新建SPI配置文件（配置对应的重试策略）
【4】RpcConfig中新增重试策略配置字段：retryStrategy
RetryStrategyKeys

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {

    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定时间间隔
     */
    String FIXED_INTERVAL = "fixedInterval";

}
RetryStrategyFactory

/**
 * 重试策略工厂（用于获取重试器对象）
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
SPI配置文件

SPI配置文件名称
com.noob.rpc.fault.retry.RetryStrategy

SPI配置文件内容
no=com.noob.rpc.fault.retry.NoRetryStrategy
fixedInterval=com.noob.rpc.fault.retry.FixedIntervalRetryStrategy
![image](https://github.com/user-attachments/assets/70b99f0c-df82-4c22-be0b-c7d2addf687e)
RpcConfig

@Data
public class RpcConfig {
    /**
     * 重试策略配置
     */
    private String retryStrategy = RetryStrategyKeys.NO;

}
相应的服务消费方的application.properties中配置重试策略
rpc.retryStrategy=fixedInterval
应用重试功能
​修改ServiceProxy逻辑，从工厂中获取重试器，将请求封装为一个Callable接口，作为重试器的参数，随后调用重试器
![image](https://github.com/user-attachments/assets/bf469b3b-d45d-42ed-b7e3-ce91801b3998)
# 十、容错机制
## 设计方案
### 容错机制
​ 容错是指系统在出现异常情况时，可以通过一定的策略保证系统仍然稳定运行，从而提高系统的可靠性和健壮性。
​ 在分布式系统中，容错机制尤为重要，因为分布式系统中的各个组件都可能存在网络故障、节点故障等各种异常情况。要顾全大局，尽可能消除偶发/单点故障对系统带来的整体影响。
​ 打个比方，将分布式系统类比为一家公司，如果公司某个优秀员工请假了，需要“触发容错”，让另一个普通员工顶上，这本质上是容错机制的一种降级策略。
​ 容错机制一般都是在系统出现错误时才触发的，需要重点学习的是容错策略和容错实现方式
### 容错策略
​ 容错策略有很多种，常用的容错策略主要是以下几个：
【1】Fail-Over 故障转移：一次调用失败后，切换一个其他节点再次进行调用，也算是一种重试
【2】Fail-Back 失败自动恢复：系统的某个功能出现调用失败或错误时，通过其他的方法，恢复该功能的正常。可以理解为降级，比如重试、调用其他服务等
【3】Fail-Safe 静默处理：系统出现部分非重要功能的异常时，直接忽略掉，不做任何处理，就像错误没有发生过一样
【4】Fail-Fast 快速失败：系统出现调用错误时，立刻报错，交给外层调用方处理
### 容错实现方式
​ 容错其实是个比较广泛的概念，除了上面几种策略外，很多技术都可以起到容错的作用。
【1】重试:重试本质上也是一种容错的降级策略，系统错误后再试一次
【2】限流:当系统压力过大、已经出现部分错误时，通过限制执行操作(接受请求)的频率或数量，对系统进行保护
【3】降级:系统出现错误后，改为执行其他更稳定可用的操作。也可以叫做“兜底”或“有损服务”，这种方式的本质是:即使牺牲一定的服务质量，也要保证系统的部分功能可用，保证基本的功能需求得到满足
【4】熔断:系统出现故障或异常时，暂时中断对该服务的请求，而是执行其他操作，以避免连锁故障
【5】超时控制:如果请求或操作长时间没处理完成，就进行中断，防止阻塞和资源占用 注意，在实际项目中，根据对系统可靠性的需求，通常会结合多种策略或方法实现容错机制。
### 容错方案设计
​ 回归到RPC框架，之前已经给系统增加重试机制，算是实现了一部分的容错能力。现在，正式引入容错机制，通过更多策略来进一步增加系统可靠性
​ 此处提供 2 种方案
【1】先容错再重试
​ 当系统发生异常时，首先会触发容错机制，比如记录日志、进行告警等，然后可以选择是否进行重试。这种方案其实是把重试当做容错机制的一种可选方案
【2】先重试再容错
​ 在发生错误后，首先尝试重试操作，如果重试多次仍然失败，则触发容错机制，比如记录日志、进行告警等。
​ 但其实这2种方案其实完全可以结合使用
​ 系统错误时，先通过重试操作解决一些临时性的异常，比如网络波动、服务端临时不可用等;如果重试多次后仍然失败，说明可能存在更严重的问题，这时可以触发其他的容错策略，比如调用降级服务、熔断、限流、快速失败等，来减少异常的影响，保障系统的稳定性和可靠性。
举个具体的例子：
（1）系统调用服务 A 出现网络错误，使用容错策略-重试
（2）重试 3 次失败后，使用其他容错策略-降级
（3）系统改为调用不依赖网络的服务 B，完成操作
基于上述构建思路完成容错机制的引入
# 实现步骤
核心构建说明（参考重试机制的引入）
【1】构建fault.tolerant包：存放容错相关的代码
【2】编写容错策略通用接口，实现不同的容错策略
【3】引入SPI机制和工厂模式支持配置和自定义容错策略扩展
## 容错策略接口
​ 容错策略通用接口：提供一个容错方法，使用 Map 类型的参数接受上下文信息(可用于灵活地传递容错处理需要用到的数据)，并且接受一个具体的异常类参数。
​ 由于容错是应用到发送请求操作的，所以容错方法的返回值是 RpcResponse(响应)
![image](https://github.com/user-attachments/assets/6eef25c3-4574-4444-a50f-348984293a8d)
## 不同容错策略介入
FailFastTolerantStrategy：快速失败容错策略
​ 遇到异常后，将异常再次抛出，交给外层处理
![image](https://github.com/user-attachments/assets/646b4864-864c-4527-b9c8-60d4c0cb308d)
## FailSafeTolerantStrategy：静默处理容错策略
​ 遇到异常后，记录一条日志，正常返回一个响应对象（就好像没有出现过报错）
![image](https://github.com/user-attachments/assets/5d160439-5b51-45e3-86ee-79cd57750a73)
FailBackTolerantStrategy：故障恢复策略
![image](https://github.com/user-attachments/assets/a1ee9f93-0e84-4a6e-8c06-35c2ed2d534f)
## FailOverTolerantStrategy：故障转移策略
​ 转移到其他服务节点
![image](https://github.com/user-attachments/assets/953770cf-a47b-40f3-b6c6-2d58880ffbb4)
## 支持配置和自定义容错策略扩展
​ 一个成熟的 RPC 框架可能会支持多种不同的容错策略，像序列化器、注册中心、负载均衡器一样，让开发者能够填写配置来指定使用的容错策略，并且支持自定义容错策略，让框架更易用、更利于扩展。其开发方式和序列化器、注册中心、负载均衡器都是一样的，都可以使用工厂创建对象、使用 SPI动态加载自定义的注册中心。
【1】引入TolerantStrategyKeys存储容错策略键名
![image](https://github.com/user-attachments/assets/ba6a5dda-698c-4706-b3d4-dd196f1a9ff7)
【2】引入TolerantStrategyFactory结合SPI机制装配不同的容错策略，编写SPI配置文件装配容错策略
![image](https://github.com/user-attachments/assets/1a85d3f6-9b01-43e3-b945-70d93c5c9a2a)
【3】RpcConfig新增容错策略配置，服务消费者可通过application.properties配置相应的容错策略
![image](https://github.com/user-attachments/assets/7636f5d4-c648-42de-988a-3b8adc19768d)
## 容错策略应用
​ 修改ServiceProxy请求处理操作，在异常处理的时候引入容错机制
![image](https://github.com/user-attachments/assets/dc68acc2-c923-4b61-89f1-ae85fa17c1a5)
# 十一、完成注解驱动
为了避免和原有代码混淆，此处单独使用springboot构建新的服务提供者、服务消费者进行构建
## Springboot Starter项目初始化
​ 新建Module（noob-rpc-springboot-starter）=》选择Springboot（Spring Initializr）=》修改Server URL（start.aliyun.com）=》JDK版本（按需选择）
image-20240415102632687image-20240415102958979
​ 选择springboot-2.6.13版本，选择依赖Spring Configuration Processor，创建项目等待依赖加载（配置maven仓库）
image-20240415103131830
​ 清理一些无用的依赖内容，引入开发好的rpc框架（noob-rpc-core）
        <!-- 引入rpc框架 -->
        <dependency>
            <groupId>com.noob.rpc</groupId>
            <artifactId>noob-rpc-core</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
## 定义注解
可参考Dubbo的注解配置
【1】@EnableDubbo:在 Spring Boot 主应用类上使用，用于启用 Dubbo 功能
【2】@DubboComponentScan:在 Spring Boot 主应用类上使用，用于指定 Dubbo 组件扫描的包路径
【3】@DubboReference:在消费者中使用，用于声明 Dubbo 服务引用
【4】@DubboService:在提供者中使用，用于声明 Dubbo 服务。
【5】@DubboMethod:在提供者和消费者中使用，用于配置 Dubbo 方法的参数、超时时间等
【6】@DubboTransported:在 Dubbo 提供者和消费者中使用，用于指定传输协议和参数，例如传输协议的类型、端口等。
​ 此处构建基础可定义3个核心注解（调通调用流程）：@EnableRpc、@RpcReference、@RpcService
@EnableRpc：用于全局标识项目需要引入RPC框架，执行初始化方法
@RpcService：服务提供者注解，在需要注册和提供服务的类上使用
@RpcReference：服务消费者注解，在需要注入服务代理对象的属性上使用
@EnableRpc
​ 考虑到消费者、提供者启动提供的初始化模块不同，因此需要在注解中指定是否需要启动web服务器。或者可以考虑将EnableRpc拆分为两个注解分别对应标识服务提供者EnableRpcProvider和服务消费者EnableRpcConsumer（但这种情况可能存在模块重复初始化的可能性）
@RpcService
​ 服务提供者注解：RpcService 注解中，需要指定服务注册信息属性，比如服务接口实现类、版本号等(也可以包括服务名称)
@RpcReference
​ 服务消费者注解：在需要注入服务代理对象的属性上使用，类似 Spring 中的 @Resource 注解 ​ RpcReference 注解中，需要指定调用服务相关的属性，比如服务接口类(可能存在多个接口)、版本号、负载均衡器、重试策略、是否 Mock 模拟调用等。
## 实现说明
​ 在starter项目中新建bootstrap包，分别针对上述3个注解新建启动类
### RpcInitBootstrap：RPC框架全局启动类
​ 在Spring框架初始化时，获取@EnableRpc注解的属性，并初始化RPC框架。可以通过实现Spring的ImportBeanDefinitionRegistrar接口，并在registerBeanDefinitions方法中获取到项目的注解和注解属性
![image](https://github.com/user-attachments/assets/6bb244d2-8d57-4055-839c-62c52e61a6a1)
注意！！！在启动netty服务器时，需重开一个线程来启动，否则会阻塞主线程，导致后续的注册操作无法完成，在之前没这个问题是因为完成所有准备工作后最后一步才启动netty服务器！！！
参考上述代码实现，从@EnableRpc注解中获取到needServer属性，从而决定是否要启动web服务器
### RpcProviderBootstrap：RPC服务提供者启动类
​ 服务提供者启动类的作用是，获取到所有包含 @RpcSenice 注解的类，并且通过注解的属性和反射机制，获取到要注册的服务信息，并且完成服务注册。
​ 怎么获取到所有包含 @Rpcservice 注解的类呢？可以主动扫描包，也可以利用 Spring 的特性监听 Bean 的加载
​ 此处选择后者，实现更简单，而且能直接获取到服务提供者类的 Bean 对象，只需要让启动类实现 BeanPostProcessor 接口的 postProcessAfterinitialization 方法，就可以在某个服务提供者 Bean 初始化后，执行注册服务等操作了。
![image](https://github.com/user-attachments/assets/f1a752db-cf33-4314-ad26-b5b0c656732d)
![image](https://github.com/user-attachments/assets/c2d820d0-e5f4-49ba-9838-b0d17de6965a)
### RpcConsumerBootstrap：RPC服务消费者启动类
​ 和服务提供者启动类的实现方式类似，在 Bean 初始化后，通过反射获取到 Bean 的所有属性，如果属性包含@RpcReference 注解，那么就为该属性动态生成代理对象并赋值。
![image](https://github.com/user-attachments/assets/2305b76a-18e4-47f3-ab1f-ce28769cb3a9)
## 注册启动类
​ 在Spring中加载已经编写好的启动类。
​ 构建说明：在用户使用@EnableRpc注解时才启动RPC框架，可以通过给EnableRpc增加@Import注解，注册自定义的启动类，实现灵活的可选加载
![image](https://github.com/user-attachments/assets/3653abd5-e413-40ba-9a62-edb1f69283dc)
## 测试
​ 构建两个springboot项目测试基于注解驱动的RPC框架
sample-springboot-provider：服务提供者
sample-springboot-consumer：服务消费者
<!-- 引入公共模块 -->
        <dependency>
            <groupId>com.noob.rpc</groupId>
            <artifactId>sample-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <!-- 引入RPC框架相关 -->
        <dependency>
            <groupId>com.noob.rpc</groupId>
            <artifactId>noob-rpc-springboot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
### sample-springboot-provider
​ 在服务提供者项目启动类上添加@EnableRpc注解
![image](https://github.com/user-attachments/assets/7c402f46-514d-4db7-8ec5-d8e4f1a6d9a6)
​ 提供一个简单的服务实例
![image](https://github.com/user-attachments/assets/bf27485b-4d54-4886-8d83-d05615e8b8c8)
### sample-springboot-consumer
​ 在服务消费者项目启动类上添加@EnableRpc注解（指定needServer属性为false）
![image](https://github.com/user-attachments/assets/b5716738-5fe9-4a5a-af4c-8545418d3e4d)
### 提供方法供测试
![image](https://github.com/user-attachments/assets/7fb83a75-e202-4e29-9873-8a442af9c788)
编写测试用例进行测试：先后启动提供者启动类、消费者启动类、执行单元测试方法（此处需注意用例方法提示错误则可能需要手动指定启动类（手动指定了启动类之后则不用单独启动指定的启动类，单元测试会自动装配））
![image](https://github.com/user-attachments/assets/f4343c14-1977-46e2-a96a-b9b1c8c3a766)




