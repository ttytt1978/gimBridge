package org.red5.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/")
public class ShowApp {


	@RequestMapping(value = "/")
    public String index() {
        return "/index.html";
    }

    @RequestMapping("/hello1")
    public    @ResponseBody  Object getList()
    {
        return "hello,springboot red5 Tomcat9 server!";
    }

    @RequestMapping("/hello2")
    public    @ResponseBody  Object getNull()
    {
        return null;
    }

    @RequestMapping("/list1")
    @ResponseBody
    public List<String> getList2()
    {
        List<String> list=new ArrayList<>();
        list.add("Hello");
        list.add("Red5");
        return list;
    }

    @RequestMapping("/book")
    @ResponseBody
    public  Book getList3()
    {
        Book book=new Book();
        book.setIsbn(344334);
        book.setName("sdkfsdkfjksdf的快速减肥独守空房");
        return book;
    }

    @RequestMapping("/books")
    @ResponseBody
    public Set<Book> getList4()
    {
        Book book1=new Book();
        book1.setIsbn(344334);
        book1.setName("sdkfsdkfjksdf的快速减肥独守空房");
        Book book2=new Book();
        book2.setIsbn(333331114);
        book2.setName("代课教师焚枯食淡");
        Set<Book> set=new HashSet<>();
        set.add(book1);
        set.add(book2);
        set.add(book1);
        return set;
    }

}
