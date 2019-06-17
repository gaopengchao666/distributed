package cn.com.netty.rpc.protocal;

import java.io.Serializable;

import lombok.Data;

/**
 * @author gaopengchao
 * 2019年6月17日
 */
@Data
public class InvokerProtocol implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String className;//类名
    private String methodName;//方法名
    private Class<?>[] paramTypes;//参数类型数组
    private Object[] params;//参数数组
}
