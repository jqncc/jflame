package org.jflame.context.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.alibaba.fastjson.JSON;

import org.jflame.toolkit.util.StringHelper;

/**
 * 转json字符串
 * 
 * @author yucan.zhang
 */
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {

    private Class<T> type;

    public JsonTypeHandler(Class<T> javaType) {
        type = javaType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String textValue = rs.getString(columnName);
        if (StringHelper.isNotEmpty(textValue)) {
            return JSON.parseObject(textValue, type);
        }
        return null;
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String textValue = rs.getString(columnIndex);
        if (StringHelper.isNotEmpty(textValue)) {
            return JSON.parseObject(textValue, type);
        }
        return null;
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String textValue = cs.getString(columnIndex);
        if (StringHelper.isNotEmpty(textValue)) {
            return JSON.parseObject(textValue, type);
        }
        return null;
    }
}
