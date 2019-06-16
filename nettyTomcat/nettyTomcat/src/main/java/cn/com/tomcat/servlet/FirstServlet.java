package cn.com.tomcat.servlet;

import cn.com.tomcat.http.MyRequest;
import cn.com.tomcat.http.MyResponse;
import cn.com.tomcat.http.MyServlet;

public class FirstServlet extends MyServlet
{

    @Override
    public void doGet(MyRequest request, MyResponse response) throws Exception
    {
        this.doPost(request, response);
    }

    @Override
    public void doPost(MyRequest request, MyResponse response) throws Exception
    {
        response.write("This is First Serlvet");
    }

}
