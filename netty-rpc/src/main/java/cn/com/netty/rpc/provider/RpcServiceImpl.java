package cn.com.netty.rpc.provider;

import cn.com.netty.rpc.api.IRpcService;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
public class RpcServiceImpl implements IRpcService
{

    @Override
    public int add(int a, int b)
    {
        return a + b;
    }

    @Override
    public int sub(int a, int b)
    {
        return a - b;
    }

    @Override
    public int mult(int a, int b)
    {
        return a * b;
    }

    @Override
    public int div(int a, int b)
    {
        return a / b;
    }

}
