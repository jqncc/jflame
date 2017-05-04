package org.jflame.mvc.support;

import org.apache.ibatis.reflection.MetaObject;

import com.baomidou.mybatisplus.mapper.MetaObjectHandler;


/**
 * 操作记录公共字段填充,createDate,createBy,updateDate,updateBy
 * @author yucan.zhang
 *
 */
public class OptLogMetaHandler extends MetaObjectHandler {

    @Override
    public void insertFill(MetaObject arg0) {
    }

    @Override
    public void updateFill(MetaObject arg0) {
    }

}
