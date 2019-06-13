package cn.com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO聊天室服务端
 * 
 * @author gaopengchao 2019年6月13日
 */
public class NIOChatServer
{
    // 服务端绑定端口
    private int port;

    // 选择器
    private Selector selector;

    private Charset charset = Charset.forName("UTF-8");

    // 用来记录在线人数，以及昵称
    private static HashSet<String> users = new HashSet<String>();

    private static String USER_EXIST = "系统提示：该昵称已经存在，请换一个昵称";

    // 相当于自定义协议格式，与客户端协商好
    private static String USER_CONTENT_SPILIT = "#@#";

    public NIOChatServer(int port) throws IOException
    {
        this.port = port;
        // 打开一个通道
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(port));
        // 配置为非阻塞
        channel.configureBlocking(false);

        selector = Selector.open();
        // 注册监听接收连接请求
        channel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务已启动，监听端口是：" + this.port);
    }

    /**
     * 监听selector
     * 
     * @throws IOException
     */
    public void listen() throws IOException
    {
        while (true)
        {
            int keyNumber = selector.select();
            // 如果没有key
            if (keyNumber == 0)
            {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext())
            {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                process(selectionKey);
            }
        }
    }

    /**
     * 处理selectKey的请求
     * 
     * @param selectionKey
     * @throws IOException
     */
    private void process(SelectionKey key) throws IOException
    {
        // 处理客户端的连接请求
        if (key.isAcceptable())
        {
            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            SocketChannel socket = channel.accept();
            // 设置非阻塞
            socket.configureBlocking(false);
            // 注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
            socket.register(selector, SelectionKey.OP_READ);
            // 将此对应的channel设置为准备接受其他客户端请求
            key.interestOps(SelectionKey.OP_ACCEPT);
            socket.write(charset.encode("请输入你的昵称:"));
        }

        // 处理客户端读数据请求
        if (key.isReadable())
        {
            // 返回该SelectionKey对应的 Channel，其中有数据需要读取
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buff = ByteBuffer.allocate(1024);
            StringBuilder content = new StringBuilder();
            try
            {
                while (client.read(buff) > 0)
                {
                    buff.flip();
                    content.append(charset.decode(buff));
                }
                // System.out.println("从IP地址为：" + sc.getRemoteAddress() +
                // "的获取到消息: " + content);
                // 将此对应的channel设置为准备下一次接受数据
                key.interestOps(SelectionKey.OP_READ);
            }
            catch(IOException io)
            {
                key.cancel();
                if (key.channel() != null)
                {
                    key.channel().close();
                }
            }
            if (content.length() > 0)
            {
                String[] arrayContent = content.toString().split(USER_CONTENT_SPILIT);
                // 注册用户
                if (arrayContent != null && arrayContent.length == 1)
                {
                    String nickName = arrayContent[0];
                    if (users.contains(nickName))
                    {
                        client.write(charset.encode(USER_EXIST));
                    }
                    else
                    {
                        users.add(nickName);
                        int onlineCount = onlineCount();
                        String message = "欢迎 " + nickName + " 进入聊天室! 当前在线人数:" + onlineCount;
                        broadCast(null, message);
                    }
                }
                // 注册完了，发送消息
                else if (arrayContent != null && arrayContent.length > 1)
                {
                    String nickName = arrayContent[0];
                    String message = content.substring(nickName.length() + USER_CONTENT_SPILIT.length());
                    message = nickName + " 说 " + message;
                    if (users.contains(nickName))
                    {
                        // 不回发给发送此内容的客户端
                        broadCast(client, message);
                    }
                }
            }

        }
    }

    public int onlineCount()
    {
        int res = 0;
        for (SelectionKey key : selector.keys())
        {
            Channel target = key.channel();

            if (target instanceof SocketChannel)
            {
                res++;
            }
        }
        return res;
    }

    public void broadCast(SocketChannel client, String content) throws IOException
    {
        // 广播数据到所有的SocketChannel中
        for (SelectionKey key : selector.keys())
        {
            Channel targetchannel = key.channel();
            // 如果client不为空，不回发给发送此内容的客户端
            if (targetchannel instanceof SocketChannel && targetchannel != client)
            {
                SocketChannel target = (SocketChannel) targetchannel;
                target.write(charset.encode(content));
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        new NIOChatServer(8080).listen();
    }
}
