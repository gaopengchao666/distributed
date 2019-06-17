package cn.com.netty.rpc.provider;

import cn.com.netty.rpc.api.IRpcHelloService;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
public class RpcHelloServiceImpl implements IRpcHelloService
{
    @Override
    public String hello(String name)
    {
        return "Hello " + name + "!";
    }

}
