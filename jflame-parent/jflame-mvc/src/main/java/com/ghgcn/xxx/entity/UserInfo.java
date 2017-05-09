package com.ghgcn.xxx.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import com.ghgcn.xxx.common.enums.SexEnum;
import com.ghgcn.xxx.common.enums.UserStatusEnum;

/**
 * <p>
 * 
 * </p>
 *
 * @author yucan.zhang
 * @since 2017-05-08
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value="user_id", type= IdType.AUTO)
	private Integer userId;
	private String userName;
	private String password;
    /**
     * 创建或注册时间
     */
	private Date createDate;
	private Date updateDate;
    /**
     * 性别0=未设置,1=男,2=女
     */
	// 指定内置枚举处理器,只能处理普通无参枚举
	//@TableField(el="sex,typeHandler=org.apache.ibatis.type.EnumOrdinalTypeHandler")
	private SexEnum sex;
	//
	private UserStatusEnum userStatus;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

    
    public SexEnum getSex() {
        return sex;
    }

    
    public void setSex(SexEnum sex) {
        this.sex = sex;
    }

    
    public UserStatusEnum getUserStatus() {
        return userStatus;
    }

    
    public void setUserStatus(UserStatusEnum userStatus) {
        this.userStatus = userStatus;
    }

}
