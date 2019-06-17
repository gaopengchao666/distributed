package cn.com.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
public class RpcRegistry
{
    private int p;//绑定端口号
    public RpcRegistry(int port)
    {
        this.p = port;
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        new RpcRegistry(8080).start();
    }

    /**
     * 服务启动
     * @throws InterruptedException 
     */
    private void start() throws InterruptedException
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup();//selector线程
        EventLoopGroup workGroup = new NioEventLoopGroup();//工作线程
        
        ServerBootstrap b = new ServerBootstrap();//serversocket
        b.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception
            {
                ChannelPipeline pipeline = ch.pipeline();
                //自定义协议解码器
                /** 入参有 5 个，分别解释如下
                maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出 TooLongFrameException。
                lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                lengthFieldLength：长度字段的长度。如：长度字段是 int 型表示，那么这个值就是 4（long 型就是 8）
                lengthAdjustment：要添加到长度字段值的补偿值
                initialBytesToStrip：从解码帧中去除的第一个字节数
                */
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
                //自定义协议编码器
                pipeline.addLast(new LengthFieldPrepender(4));
                //对象参数类型编码器
                pipeline.addLast("encoder",new ObjectEncoder());
                //对象参数类型解码器
                pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
                
                pipeline.addLast(new RegistryHandler());
                        
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = b.bind(p).sync();
        System.out.println("My RPC Registry start listen : " + p);
        future.channel().closeFuture().sync();
    }
}
