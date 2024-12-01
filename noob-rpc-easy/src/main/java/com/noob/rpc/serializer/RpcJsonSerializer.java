package com.noob.rpc.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noob.rpc.pojo.RpcRequest;
import com.noob.rpc.pojo.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.io.IOException;

/**
 * 自定义 Json 序列化器
 */
public class RpcJsonSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册 HttpMethod 的序列化器
        SimpleModule module = new SimpleModule();
        module.addSerializer(HttpMethod.class, new HttpMethodSerializer());
        // 为 DefaultFullHttpRequest 添加反序列化器
        module.addDeserializer(DefaultFullHttpRequest.class, new DefaultFullHttpRequestDeserializer());
        OBJECT_MAPPER.registerModule(module);
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, classType);
        if (obj instanceof RpcRequest) {
            return handleRequest((RpcRequest) obj, classType);
        }
        if (obj instanceof RpcResponse) {
            return handleResponse((RpcResponse) obj, classType);
        }
        return obj;
    }

    /**
     * 处理 RpcRequest 的反序列化
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 处理 RpcResponse 的反序列化
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }

    // 自定义 HttpMethod 序列化器
    private static class HttpMethodSerializer extends JsonSerializer<HttpMethod> {
        @Override
        public void serialize(HttpMethod value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.name()); // 将 HttpMethod 枚举值序列化为字符串
        }
    }

    // 自定义 DefaultFullHttpRequest 反序列化器
    private static class DefaultFullHttpRequestDeserializer extends JsonDeserializer<DefaultFullHttpRequest> {

        @Override
        public DefaultFullHttpRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            System.out.println("开始反序列化");
            // 读取 JSON 对象
            ObjectNode node = p.getCodec().readTree(p);

            // 获取 HTTP 请求方法和 URI
            String method = node.get("method").asText();
            System.out.println("方法是"+method);
            String uri = node.get("uri").asText();
            JsonNode contentNode = node.get("content");
            System.out.println("内容是"+contentNode);

            HttpVersion httpVersion = HttpVersion.HTTP_1_1;

            // 获取 HttpMethod 枚举值
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            // 处理 content 字段：解析为字节数组
            ByteBuf byteBufContent = Unpooled.EMPTY_BUFFER;
            if (contentNode != null && contentNode.isArray()) {
                // 如果 content 是字节数组的 JSON 表示（例如：[1,2,3]），解析为 byte[]
                byte[] contentBytes = new byte[contentNode.size()];
                for (int i = 0; i < contentNode.size(); i++) {
                    contentBytes[i] = (byte) contentNode.get(i).asInt();
                }
                byteBufContent = Unpooled.wrappedBuffer(contentBytes); // 将字节数组转换为 ByteBuf
            }
            // 构造 DefaultFullHttpRequest 对象
            return new DefaultFullHttpRequest(httpVersion, httpMethod, uri,byteBufContent);
        }
    }

}
