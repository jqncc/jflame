package org.jflame.mvc.support.mybatis;

import java.util.Date;

import org.apache.ibatis.reflection.MetaObject;

import com.baomidou.mybatisplus.mapper.MetaObjectHandler;


/**
 * 操作记录公共字段填充,createDate,createBy,updateDate,updateBy
 * @author yucan.zhang
 *
 */
public class OptLogMetaHandler extends MetaObjectHandler {
    private final String fillCreateDate="createDate";
    private final String fillUpdateDate="updateDate";
    private final String fillCreateBy="createBy";
    private final String fillUpdateBy="updateBy";

    @Override
    public void insertFill(MetaObject arg0) {
        Date now = new Date();
        if (arg0.hasGetter(fillCreateDate)) {
            if (arg0.getValue(fillCreateDate) == null) {
                arg0.setValue(fillCreateDate, now);
            }
        }
        if (arg0.hasGetter(fillUpdateDate)) {
            if (arg0.getValue(fillUpdateDate) == null) {
                arg0.setValue(fillUpdateDate, now);
            }
        }
    }

    @Override
    public void updateFill(MetaObject arg0) {
        if (arg0.hasGetter(fillUpdateDate)) {
            if (arg0.getValue(fillUpdateDate) == null) {
                arg0.setValue(fillUpdateDate, new Date());
            }
        }
    }

}
