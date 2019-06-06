package cn.com.rpc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author gaopengchao
 * 2019年6月6日
 */
public class RpcProxyServer
{
    ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 发布服务
     * @param helloService
     * @param i
     */
    public void publisher(Object service, int i)
    {
        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(i);
            while(true)
            {
                Socket socket = serverSocket.accept(); 
                //每一个socket 交给一个processorHandler来处理ProcessorHander
                executor.execute(new ProcessorHandler(socket,service));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
