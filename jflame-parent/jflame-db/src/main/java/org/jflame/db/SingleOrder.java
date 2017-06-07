package org.jflame.db;

import java.io.Serializable;

import org.jflame.toolkit.common.bean.PageBean.OrderEnum;

/**
 * 单一排序条件对象
 * 
 * @author zyc
 */
public class SingleOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    private OrderEnum orderType;
    private String orderBy;

    public SingleOrder() {
    }

    public SingleOrder(String _orderBy, OrderEnum _orderType) {
        orderBy = _orderBy;
        orderType = _orderType;
    }

    public OrderEnum getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderEnum orderType) {
        this.orderType = orderType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public String toString() {
        return orderBy + " " + orderType;
    }

}
