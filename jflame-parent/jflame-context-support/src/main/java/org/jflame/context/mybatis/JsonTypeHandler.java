package org.jflame.context.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import org.jflame.commons.json.JsonHelper;
import org.jflame.commons.util.StringHelper;

/**
 * 转json字符串
 * 
 * @author yucan.zhang
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {

    private Class<T> type;

    public JsonTypeHandler(Class<T> javaType) {
        type = javaType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JsonHelper.toJson(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String textValue = rs.getString(columnName);
        if (StringHelper.isNotEmpty(textValue)) {
            return JsonHelper.parseObject(textValue, type);
        }
        return null;
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String textValue = rs.getString(columnIndex);
        if (StringHelper.isNotEmpty(textValue)) {
            return JsonHelper.parseObject(textValue, type);
        }
        return null;
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String textValue = cs.getString(columnIndex);
        if (StringHelper.isNotEmpty(textValue)) {
            return JsonHelper.parseObject(textValue, type);
        }
        return null;
    }
}
