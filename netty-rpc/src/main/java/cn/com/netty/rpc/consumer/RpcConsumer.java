package cn.com.netty.rpc.consumer;

import cn.com.netty.rpc.api.IRpcHelloService;
import cn.com.netty.rpc.api.IRpcService;
import cn.com.netty.rpc.consumer.proxy.RpcProxy;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
public class RpcConsumer
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        IRpcHelloService rpcHello = RpcProxy.create(IRpcHelloService.class);
        System.out.println(rpcHello.hello("gpc"));
        
        IRpcService service = RpcProxy.create(IRpcService.class);
        System.out.println(service.add(6, 2));
        System.out.println(service.sub(6, 2));
        System.out.println(service.mult(6, 2));
        System.out.println(service.div(6, 2));
    }

}
