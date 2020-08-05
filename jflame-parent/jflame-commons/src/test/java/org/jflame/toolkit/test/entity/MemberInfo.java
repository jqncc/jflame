package org.jflame.toolkit.test.entity;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;

import org.jflame.commons.codec.TranscodeHelper;
import org.jflame.commons.util.StringHelper;

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

    // @Transient
    public String getPasswd() {
        return passwd;
    }

    @Transient
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
            return TranscodeHelper.urlDecode(nickName);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MemberInfo [");
        if (userId != null) {
            builder.append("userId=")
                    .append(userId)
                    .append(", ");
        }
        if (userName != null) {
            builder.append("userName=")
                    .append(userName)
                    .append(", ");
        }
        if (passwd != null) {
            builder.append("passwd=")
                    .append(passwd)
                    .append(", ");
        }
        if (userMobile != null) {
            builder.append("userMobile=")
                    .append(userMobile)
                    .append(", ");
        }
        if (sex != null) {
            builder.append("sex=")
                    .append(sex)
                    .append(", ");
        }
        if (userStatus != null) {
            builder.append("userStatus=")
                    .append(userStatus)
                    .append(", ");
        }
        if (nickName != null) {
            builder.append("nickName=")
                    .append(nickName)
                    .append(", ");
        }
        if (userEmail != null) {
            builder.append("userEmail=")
                    .append(userEmail)
                    .append(", ");
        }
        if (headImage != null) {
            builder.append("headImage=")
                    .append(headImage)
                    .append(", ");
        }
        if (realName != null) {
            builder.append("realName=")
                    .append(realName)
                    .append(", ");
        }
        if (birthday != null) {
            builder.append("birthday=")
                    .append(birthday)
                    .append(", ");
        }
        if (idcard != null) {
            builder.append("idcard=")
                    .append(idcard)
                    .append(", ");
        }
        if (age != null) {
            builder.append("age=")
                    .append(age)
                    .append(", ");
        }
        if (appNo != null) {
            builder.append("appNo=")
                    .append(appNo);
        }
        builder.append("]");
        return builder.toString();
    }

}
