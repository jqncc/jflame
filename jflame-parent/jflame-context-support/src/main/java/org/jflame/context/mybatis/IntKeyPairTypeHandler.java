package org.jflame.context.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import org.jflame.commons.common.bean.pair.IIntKeyPair;

/**
 * 实现继承IIntKeyPair的枚举类型与数据库int类型转换,具体类型必须在注解@MappedTypes上指定
 * 
 * @author yucan.zhang
 * @param <E>
 */
// @MappedTypes(value = { IIntKeyPair.class })
@MappedJdbcTypes(value = { JdbcType.INTEGER,JdbcType.TINYINT,JdbcType.SMALLINT })
@SuppressWarnings("rawtypes")
public class IntKeyPairTypeHandler<E extends Enum & IIntKeyPair> extends BaseTypeHandler<E> {

    private Map<Integer,E> objs = new HashMap<>();

    public IntKeyPairTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        E[] enums = type.getEnumConstants();
        if (enums == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
        for (E e : enums) {
            IIntKeyPair valuedEnum = (IIntKeyPair) e;
            objs.put(valuedEnum.getKey(), e);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        IIntKeyPair pair = (IIntKeyPair) parameter;
        ps.setInt(i, pair.getKey());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int i = rs.getInt(columnName);
        return objs.get(i);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int i = rs.getInt(columnIndex);
        return objs.get(i);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int i = cs.getInt(columnIndex);
        return objs.get(i);
    }
}
