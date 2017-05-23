package cn.huaxunchina.common.jdbcsupport;

import cn.huaxunchina.toolkit.common.dto.PageBean.OrderEnum;

/**
 * 单一排序条件对象
 * 
 * @author zyc
 */
public class SingleOrder
{
    private OrderEnum orderType;
    private String orderBy;

    public SingleOrder()
    {
    }

    public SingleOrder(String _orderBy, OrderEnum _orderType)
    {
        orderBy = _orderBy;
        orderType = _orderType;
    }

    public OrderEnum getOrderType()
    {
        return orderType;
    }

    public void setOrderType(OrderEnum orderType)
    {
        this.orderType = orderType;
    }

    public String getOrderBy()
    {
        return orderBy;
    }

    public void setOrderBy(String orderBy)
    {
        this.orderBy = orderBy;
    }

    @Override
    public String toString()
    {
        return orderBy + " " + orderType;
    }

}
