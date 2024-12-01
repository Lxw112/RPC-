一、静态代理实现
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
