package cn.com.netty.rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.netty.rpc.protocal.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author gaopengchao 2019年6月17日
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter
{
    // 保存所有可用服务
    private static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    // 保存所有服务类名
    private List<String> classNames = new ArrayList<String>();

    public RegistryHandler()
    {
        scannerClass("cn.com.netty.rpc.provider");
        doRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;
        // 当客户端建立连接时，需要从自定义协议中获取信息，拿到具体的服务和实参 使用反射调用
        if (registryMap.containsKey(request.getClassName()))
        {
            Object instance = registryMap.get(request.getClassName());
            Method method = instance.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            result = method.invoke(instance, request.getParams());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        ctx.close();
    }

    /**
     * 扫描所有的服务类
     */
    private void scannerClass(String packageName)
    {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles())
        {
            if (file.isDirectory())
            {
                scannerClass(packageName + "." + file.getName());
            }
            else
            {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    /**
     * 注册服务
     */
    private void doRegistry()
    {
        if (classNames.isEmpty())
        {
            return;
        }
        try
        {
            for (String className : classNames)
            {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
