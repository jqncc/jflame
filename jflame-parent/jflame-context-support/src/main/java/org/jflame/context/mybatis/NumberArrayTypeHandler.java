package org.jflame.context.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import org.jflame.toolkit.convert.mutual.StringAndNumberArrayConverter;

/**
 * mybatis类型处理器,数字数组与字符串转换, 如:"1,2,3"=&gt;new Integer[]{1,2,3}
 * 
 * @author yucan.zhang
 */
@MappedJdbcTypes(value = { JdbcType.VARCHAR })
public class NumberArrayTypeHandler extends BaseTypeHandler<Number[]> {

    private final StringAndNumberArrayConverter<? extends Number> converter;

    @SuppressWarnings("unchecked")
    public NumberArrayTypeHandler(Class<? extends Number[]> type) {
        if (type == null) {
            throw new IllegalArgumentException("parameter 'Type' cannot be null");
        }
        converter = new StringAndNumberArrayConverter<>((Class<? extends Number>) type.getComponentType());
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Number[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, converter.inverseConvert(parameter));
    }

    @Override
    public Number[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String text = rs.getString(columnName);
        return converter.convert(text);
    }

    @Override
    public Number[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String text = rs.getString(columnIndex);
        return converter.convert(text);
    }

    @Override
    public Number[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String text = cs.getString(columnIndex);
        return converter.convert(text);
    }

}