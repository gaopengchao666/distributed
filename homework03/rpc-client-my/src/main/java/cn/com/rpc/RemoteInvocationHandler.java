package cn.com.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.com.rpc.RpcRequest;

/**
 * @author gaopengchao
 * 2019年6月6日
 */
public class RemoteInvocationHandler implements InvocationHandler
{
    private String host;
    private int port;
    public RemoteInvocationHandler(String host,int port)
    {
        this.host = host;
        this.port = port;
    }

    /**
     * 调用服务类
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        //请求会进入到这里
        System.out.println("come in");
        RpcRequest request = new RpcRequest();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        
        RpcNetTransport netTransport = new RpcNetTransport(host,port);
        Object object = netTransport.send(request);
        return object;
    }

}
