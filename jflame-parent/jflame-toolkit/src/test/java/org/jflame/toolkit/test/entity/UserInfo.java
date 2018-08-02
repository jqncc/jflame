package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jflame.toolkit.valid.DynamicValid;
import org.jflame.toolkit.valid.DynamicValid.ValidRule;
import org.jflame.toolkit.valid.EqField;

@EqField(field = "password", eqField = "confirmPwd", message = "两次密码不一致")
public class UserInfo implements Serializable {
 
    private static final long serialVersionUID = -1845840272344274024L;
    
    @NotNull
    @DynamicValid(rules= {ValidRule.letterNumOrline},message="用户名由字母数字或下划线组成",nullable=true)
    private String userName;
    @NotNull
    private String password;
    private String confirmPwd;
    @NotNull
    private Date birthday;
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPwd() {
        return confirmPwd;
    }
    
    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }
    
    public Date getBirthday() {
        return birthday;
    }
    
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    
    

}
