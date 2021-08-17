package com.dao;

import com.bean.User;

public interface UserDao {
    public int add(User user);
    public String getPasswordByName(String name);
}
