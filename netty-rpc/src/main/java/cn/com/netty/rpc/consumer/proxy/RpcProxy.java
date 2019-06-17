package cn.com.netty.rpc.consumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.com.netty.rpc.protocal.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
public class RpcProxy
{
    //jdk创建代理类
    public static <T> T create(Class<?> clazz)
    {
        MethodProxy methodProxy = new MethodProxy(clazz);
        @SuppressWarnings("unchecked")
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, methodProxy);
        return result;
    }
    
    public static class MethodProxy implements InvocationHandler
    {
        private Class<?> clazz;
        public MethodProxy(Class<?> clazz)
        {
            this.clazz = clazz;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            //如果传进来是一个已经实现接口的实现类，则无需远程调用
            if (Object.class.equals(method.getDeclaringClass()))
            {
                return method.invoke(this,args);
            }
            else
            {
                //传进来是一个接口
                return rpcInvoke(proxy,method,args);
            }
        }

        /**
         * rpc远程调用
         * @throws InterruptedException 
         */
        private Object rpcInvoke(Object proxy, Method method, Object[] args) throws InterruptedException
        {
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(args);
            msg.setParamTypes(method.getParameterTypes());
            
            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            
            EventLoopGroup group = new NioEventLoopGroup();
            
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //自定义协议解码器
                            /** 入参有5个，分别解释如下
                             maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                             lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                             lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                             lengthAdjustment：要添加到长度字段值的补偿值
                             initialBytesToStrip：从解码帧中去除的第一个字节数
                             */
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            //自定义协议编码器
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            //对象参数类型编码器
                            pipeline.addLast("encoder", new ObjectEncoder());
                            //对象参数类型解码器
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast("handler",consumerHandler);
                        }
                    });
            
            ChannelFuture future = b.connect("localhost", 8080).sync();
            future.channel().writeAndFlush(msg).sync();
            future.channel().closeFuture().sync();
            return consumerHandler.getResponse();
        }
        
    }
}
