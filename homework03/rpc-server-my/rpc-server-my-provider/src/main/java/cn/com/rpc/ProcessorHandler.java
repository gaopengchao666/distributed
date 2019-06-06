package cn.com.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import cn.com.rpc.RpcRequest;

/**
 * 消息处理
 * @author gaopengchao
 * 2019年6月6日
 */
public class ProcessorHandler implements Runnable
{
    private Object service;
    private Socket socket;
    public ProcessorHandler(Socket socket,Object service)
    {
        this.socket = socket;
        this.service = service;
    }
    
    
    @Override
    public void run()
    {
        ObjectInputStream objectInputStream=null;
        ObjectOutputStream objectOutputStream=null;
        try
        {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            RpcRequest request = (RpcRequest) objectInputStream.readObject();
            Object result = invoke(request);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(result);
            
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch(InvocationTargetException e)
        {
            e.printStackTrace();
        }
        finally 
        {
            try
            {
                if (objectInputStream != null)
                {
                    objectInputStream.close();
                }
                if (objectOutputStream != null)
                {
                    objectOutputStream.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    /**
     * 通过反射调用具体服务
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     */
    private Object invoke(RpcRequest request) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object[] parameters = request.getParameters();
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i=0;i<parameters.length;i++)
        {
            types[i] = parameters[i].getClass();
        }
        
        Class clazz = Class.forName(request.getClassName());
        Method method = clazz.getMethod(request.getMethodName(), types);
        Object result = method.invoke(service, parameters);
        return result;
    }
}
