package com.maincontroller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class MainController {
//
//    @Autowired
//    YhService yhService;
//
//    @RequestMapping("/QueryYhList")
//    @ResponseBody
//    public List<YhList> queryYhList(int pageNumber, int pageSize)
//    {
//        PageInfo<YhList> list= yhService.getYhList(null,null,null,null,null,pageNumber,pageSize);
//        return list.getList();
//    }
//
//    @RequestMapping("/YhList")
//    @ResponseBody
//    public List<String> getList2()
//    {
//        List<String> list=new ArrayList<>();
//        list.add("Hello");
//        list.add("Red5");
//        return list;
//    }




}
