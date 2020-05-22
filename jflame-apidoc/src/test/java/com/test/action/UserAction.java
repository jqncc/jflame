package com.test.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.model.User;

/**
 * 用户controller
 * 
 * @module 用户管理
 * @author charles.zhang
 *
 */
@Controller
@RequestMapping(value = "x", method = RequestMethod.POST)
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

    /**
     * 查找用户
     * 
     * @param userName 用户名
     * @param age 年龄
     * @param weight 体重
     * @param skin 收集皮肤
     * @param request
     */
    @GetMapping("findUser")
    @ResponseBody
    public void findUser(@RequestParam(name = "uname", defaultValue = "a", required = true) String userName,
            Integer age, int weight, String[] skin, HttpServletRequest request) {

    }
}
