package com.ghgcn.xxx.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * <p>
 * 
 * </p>
 *
 * @author yucan.zhang
 * @since 2017-05-08
 */
public class SysFunction extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value="fun_id", type= IdType.AUTO)
	private Integer funId;
    /**
     * 功能权限标识
     */
	private String funCode;
    /**
     * 功能名称
     */
	private String funName;
    /**
     * 1=菜单,2=功能
     */
	private Integer funType;
    /**
     * 直接父功能id
     */
	private int parentId=0;
    /**
     * 所有父级功能id
     */
	@TableField(value="parent_ids",el="parentIds,typeHandler=org.jflame.mvc.support.mybatis.IdsTypeHandler")
	private Integer[] parentIds;
	private String funUrl;
	private String funDesc;
    /**
     * 排序
     */
	private int orderNum;
    /**
     * 指定图标名称，作为菜单时，可以有不同图标
     */
	private String menuIco;


	public Integer getFunId() {
		return funId;
	}

	public void setFunId(Integer funId) {
		this.funId = funId;
	}

	public String getFunCode() {
		return funCode;
	}

	public void setFunCode(String funCode) {
		this.funCode = funCode;
	}

	public String getFunName() {
		return funName;
	}

	public void setFunName(String funName) {
		this.funName = funName;
	}

	public Integer getFunType() {
		return funType;
	}

	public void setFunType(Integer funType) {
		this.funType = funType;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public Integer[] getParentIds() {
		return parentIds;
	}

	public void setParentIds(Integer[] parentIds) {
		this.parentIds = parentIds;
	}

	public String getFunUrl() {
		return funUrl;
	}

	public void setFunUrl(String funUrl) {
		this.funUrl = funUrl;
	}

	public String getFunDesc() {
		return funDesc;
	}

	public void setFunDesc(String funDesc) {
		this.funDesc = funDesc;
	}

	public int getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}

	public String getMenuIco() {
		return menuIco;
	}

	public void setMenuIco(String menuIco) {
		this.menuIco = menuIco;
	}

}
