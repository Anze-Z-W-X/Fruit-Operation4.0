package com.myssm.myspringmvc;

import com.myssm.io.BeanFactory;
import com.myssm.io.ClassPathXmlApplicationContext;
import com.myssm.util.StringUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@WebServlet("*.do")
public class DispatcherServlet extends ViewBaseServlet{
    private BeanFactory beanFactory;

    public DispatcherServlet(){
    }

    public void init() throws ServletException {
        super.init();
        beanFactory=new ClassPathXmlApplicationContext();

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //设置编码
        request.setCharacterEncoding("UTF-8");
        //假设url是：  http://localhost:8080/pro15/hello.do
        //那么servletPath是：    /hello.do
        // 我的思路是：
        // 第1步： /hello.do ->   hello   或者  /fruit.do  -> fruit
        // 第2步： hello -> HelloController 或者 fruit -> FruitController
        String servletPath = request.getServletPath();
        servletPath = servletPath.substring(1);
        int lastDotIndex = servletPath.lastIndexOf(".do") ;
        servletPath = servletPath.substring(0,lastDotIndex);

        Object controllerBeanObj = beanFactory.getBean(servletPath);

        String operate = request.getParameter("operate");
        if(StringUtil.isEmpty(operate)){
            operate = "index" ;
        }

        try {
            Method[] methods = controllerBeanObj.getClass().getDeclaredMethods();
            for(Method method:methods){
                if(operate.equals(method.getName())) {
                    Parameter[] parameters = method.getParameters();
                    Object[] parameterValues = new Object[parameters.length];
                    for(int i=0;i<parameters.length;i++){
                        Parameter parameter = parameters[i];
                        String parameterName = parameter.getName();
                        if(parameterName.equals("request"))parameterValues[i]=request;
                        else if(parameterName.equals("response"))parameterValues[i]=response;
                        else if(parameterName.equals("seesion"))parameterValues[i]=request.getSession();
                        else {
                            String parameterValue = request.getParameter(parameterName);
                            String typeName = parameter.getType().getName();
                            Object parameterObj = parameterValue;
                            if(parameterObj!=null&&"java.lang.Integer".equals(typeName)){
                                parameterObj = Integer.parseInt(parameterValue);
                            }
                            parameterValues[i]=parameterObj;
                        }
                    }


                    method.setAccessible(true);
                    Object returnObj = method.invoke(controllerBeanObj, parameterValues);

                    //视图处理
                    String methodReturnStr = (String) returnObj;
                    if (methodReturnStr.startsWith("redirect:")) {        //比如：  redirect:fruit.do
                        String redirectStr = methodReturnStr.substring("redirect:".length());
                        response.sendRedirect(redirectStr);
                    }
                    else {
                        super.processTemplate(methodReturnStr, request, response);    // 比如：  "edit"
                    }
                }

            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

// 常见错误： IllegalArgumentException: argument type mismatch