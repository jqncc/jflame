package com.test.action;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.model.User;

@Controller
@RequestMapping(value = { "x","b" }, method = RequestMethod.POST)
public class UserAction {

    /**
     * 根据id获取用户
     * 
     * @param id 用户id
     * @return
     */
    @RequestMapping({ "x","y" })
    @ResponseBody
    public User getUser(List<Integer> id) {
        User user = new User();
        user.setAge(1);
        user.setUserId(111111222);
        user.setUserName("username1");
        return user;
    }

    /**
     * 保存用户
     * 
     * @param user
     */
    public void saveUser(User user) {

    }
}
