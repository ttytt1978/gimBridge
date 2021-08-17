package com.service.impl;

import com.bean.User;
import com.dao.UserDao;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    public int add(User user){
        return userDao.add(user);
    }
    public String getPasswordByName(String name){
        return userDao.getPasswordByName(name);
    }
}
