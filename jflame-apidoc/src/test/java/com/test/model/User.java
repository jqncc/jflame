package com.test.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 用户信息
 * 
 * @version 1.0
 * @author yucan.zhang
 */
@XmlRootElement(name = "xmlname")
public class User {

    private Integer userId;
    /**
     * 用户名
     */
    @NotNull
    private String userName;
    /**
     * 年龄
     */
    private int age;
    /**
     * 生日
     */
    private Date birthday;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @NotNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

}
