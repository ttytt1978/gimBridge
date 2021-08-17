package com.service;

import com.bean.User;

public interface UserService {
    public int add(User user);
    public String getPasswordByName(String name);
}
