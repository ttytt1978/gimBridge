package com.convert;


import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.awt.image.BufferedImage;
import java.io.Serializable;


//@ControllerAdvice//(basePackages = "com.controller")
@ResponseBody
public class CustomResponseAdvice implements ResponseBodyAdvice<Object> {


    public class Output implements Serializable
    {
        public boolean success;
        public Object object;
        public String message;

        public Output()
        {

        }

        public boolean getSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }



    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        System.out.println("序列化，对象进来了。");
        if(null!=obj)
            System.out.println(obj.getClass());
        if(obj==null)
            return null;
        if( obj instanceof BufferedImage)//图像数据
            return obj;
        if(obj.getClass().isArray())//字节数组
            return obj;
        Output result=new Output();
        if(obj instanceof Exception)
        {
            Exception exception=(Exception)obj;
            result.success=false;
            result.object=null;
            result.message=exception.getMessage();
        }else
        {
            result.success=true;
            result.message=null;
            result.object=obj;
        }
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Exception handleException(Exception exception) {
        System.out.println("序列化，异常进来了。");
        return exception;
    }

}