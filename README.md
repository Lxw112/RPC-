![image](https://github.com/user-attachments/assets/21660df9-4eca-4edc-a765-1f44691a511c)一、静态代理实现
​ 静态代理是指为每一个特定类型的接口或者对象提供一个代理类进行代理操作，此处创建UserServiceProxy，实现代理方法。

​ 此处代理方法的实现，不是直接复制那段UserServicelmpl相关的代码，而是通过Http请求去调用接口服务（此处需注意参数的序列化处理）

构建步骤说明：

【1】pom.xml中引入noob-rpc-easy依赖（需用到序列化器）

【2】自定义UserServiceProxy代理类实现代理

【3】修改调用方法：将UserService实现交由UserServiceProxy代理处理
总结：静态代理其实就是变相实现UserService接口，只不过其代理操作核心是通过http调用对应的服务接口。但是基于这种方式，如果后续有多个接口需要调用，则需要相应编写不同的代理类，这种代理方式的灵活性会非常差，因此引入动态代理进行构建
二、动态代理实现
​ 动态代理的作用：根据要生成的对象的类型，自动生成一个代理对象。此处使用JDK动态代理实现
​ 常用的动态代理实现方式有JDK动态代理和基于字节码生成的动态代理(比如CGLIB)。前者简单易用、无需引入额外的库，但缺点是只能对接口进行代理；后者更灵活、可以对任何类进行代理，但性能略低于JDK动态代理。
构建步骤说明：
【1】在noob-easy-rpc模块中编写动态代理类ServiceProxy（实现InvocationHandler的invoke方法，此处对比静态代理的实现内容基本大同小异）
【2】使用工厂设计模式，简化对象创建过程
【3】在sample-consumer中EasyConsumerSample修改代理模式
三、全局配置加载
维护全局配置对象
​ RPC框架中需要维护一个全局的配置对象。在引入RPC框架的项目启动时，从配置文件中读取配置并创建对象实例，之后就可以集中地从这个对象中获取配置信息，而不用每次加载配置时再重新读取配置、并创建新的对象，减少了性能开销。
​ 使用设计模式中的单例模式，就能够很轻松地实现这个需求了。一般情况下，使用holder来维护全局配置对象实例。项目中，可以使用RpcApplication类作为RPC项目的启动入口、并且维护项目全局用到的变量。
RpcApplication
​ 双检锁单例模式实现：支持在获取配置时才调用init方法实现懒加载。可支持传入配置对象，如果不传入默认引入ConfigUtils来加载配置，基于这种设计只需要一行代码即可加载配置
RpcConfig rpc = RpcApplication.getRpcConfig();
---实现了支持读取application.yaml格式的配置文件
四、接口Mock实现
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
五、序列化器与SPI机制
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
