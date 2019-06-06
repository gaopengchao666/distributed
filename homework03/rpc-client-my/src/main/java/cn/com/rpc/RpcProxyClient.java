package cn.com.rpc;

import java.lang.reflect.Proxy;

/**
 * 客户端请求代理类
 * @author gaopengchao
 * 2019年6月6日
 */
public class RpcProxyClient
{
    public <T> T clientProxy(Class<T> interfaces,String host,int port)
    {
        return (T) Proxy.newProxyInstance(interfaces.getClassLoader(), new Class<?>[]{interfaces}, new RemoteInvocationHandler(host,port));
    }
}
