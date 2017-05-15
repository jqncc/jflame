package org.jflame.mvc.support.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.jflame.toolkit.util.StringHelper;


/**
 * id数组与逗号分隔的id字符串转换.如:"1,2,3,33"=>new Integer[]{1,2,3,33}
 * 
 * @author yucan.zhang
 *
 */
@MappedTypes(value = {Integer[].class})
@MappedJdbcTypes(value = { JdbcType.VARCHAR })
public class IdsTypeHandler extends BaseTypeHandler<Integer[]> {
    private final String splitChar=",";
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Integer[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, StringHelper.join(parameter, splitChar));
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String ids=rs.getString(columnName);
        String[] idArr=ids.split(splitChar);
        return convertIntArray(idArr);
    }

    @Override
    public Integer[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String ids=rs.getString(columnIndex);
        String[] idArr=ids.split(splitChar);
        return convertIntArray(idArr);
    }

    @Override
    public Integer[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String ids=cs.getString(columnIndex);
        String[] idArr=ids.split(splitChar);
        return convertIntArray(idArr);
    }
    
    private Integer[] convertIntArray(String[] idArr) {
        List<Integer> idList=new ArrayList<>(idArr.length);
        for (String id : idArr) {
            if (StringHelper.isNotEmpty(id)) {
                idList.add(Integer.valueOf(id));
            }
        }
        return idList.toArray(new Integer[idList.size()]);
    }
}
