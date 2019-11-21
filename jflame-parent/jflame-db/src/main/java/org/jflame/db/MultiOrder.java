package org.jflame.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.common.bean.PageBean.OrderEnum;

public class MultiOrder implements Serializable {

    private static final long serialVersionUID = 2186863489574274335L;
    private Set<SingleOrder> orders = new HashSet<>(2);

    public MultiOrder() {
    }

    public MultiOrder(String orderBy, OrderEnum orderEnum) {
        orders.add(new SingleOrder(orderBy, orderEnum));
    }

    public void add(String orderBy, OrderEnum orderEnum) {
        orders.add(new SingleOrder(orderBy, orderEnum));
    }

    public void add(SingleOrder order) {
        orders.add(order);
    }

    @Override
    public String toString() {
        return StringUtils.join(orders, ",");
    }

}
