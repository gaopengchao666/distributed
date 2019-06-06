package cn.com.rpc;

import cn.com.rpc.IHelloService;
import cn.com.rpc.User;

/**
 * @author gaopengchao
 * 2019年6月6日
 */
public class HelloServiceImpl implements IHelloService
{
    /**
     * 实现接口方法
     */
    @Override
    public String sayHello(String content)
    {
        System.out.println("Service receive message:" + content);
        return content;
    }

    /**
     * 保存用户
     */
    @Override
    public String saveUser(User user)
    {
        return null;
    }
}
