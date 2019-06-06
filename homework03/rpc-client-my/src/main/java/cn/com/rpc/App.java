package cn.com.rpc;

import cn.com.rpc.IHelloService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        RpcProxyClient client = new RpcProxyClient();
        IHelloService service = client.clientProxy(IHelloService.class, "localhost", 8080);
        String result = service.sayHello("success");
        System.out.println(result);
    }
}
