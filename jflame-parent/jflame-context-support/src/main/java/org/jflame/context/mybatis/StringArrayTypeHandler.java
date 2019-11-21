package org.jflame.context.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import org.jflame.commons.util.StringHelper;

/**
 * mybatis类型处理器,字符串数组与字符串转换, 如:"a,b,c"=&gt;{"a","b","c"}
 * 
 * @author yucan.zhang
 */
@MappedTypes({ String[].class })
@MappedJdbcTypes(value = { JdbcType.VARCHAR })
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, StringHelper.join(parameter));
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String text = rs.getString(columnName);
        return convertToStringArray(text);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String text = rs.getString(columnIndex);
        return convertToStringArray(text);
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String text = cs.getString(columnIndex);
        return convertToStringArray(text);
    }

    private String[] convertToStringArray(String text) {
        if (StringHelper.isNotEmpty(text)) {
            return StringHelper.split(text);
        }
        return null;
    }
}