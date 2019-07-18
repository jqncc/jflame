package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.util.Date;

import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 用户基础信息表
 *
 * @author yucan.zhang
 * @since 2017-05-23
 */
public class MemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String userId;
    private String userName;
    private String passwd;

    private String userMobile;
    /**
     * 性别0=未设置,1=男,2=女
     */
    private Integer sex;
    /**
     * 0=禁用,1=启用,2=锁定
     */
    private Integer userStatus;

    /**
     * 昵称
     */
    private String nickName;
    /**
     * 邮箱地址
     */
    private String userEmail;
    /**
     * 用户头像图片路径
     */
    private String headImage;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 出生日期
     */
    private Date birthday;
    /**
     * 身份证号
     */
    private String idcard;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 注册来源应用
     */
    private String appNo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public String getNickName() {
        if (StringHelper.isNotEmpty(nickName)) {
            return TranscodeHelper.urldecode(nickName);
        }
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    // @Transient
    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAppNo() {
        return appNo;
    }

    public void setAppNo(String appNo) {
        this.appNo = appNo;
    }

}
