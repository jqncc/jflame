package cn.huaxunchina.common.jdbcsupport;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

public class AnnonBeanPropertyRowMapper<T> implements RowMapper<T>
{
    private Class<T> mappedClass;
    private Map<String, PropertyDescriptor> mappedFields;
    private TableEntityMetaData metaData;

    public AnnonBeanPropertyRowMapper()
    {
    }

    public AnnonBeanPropertyRowMapper(Class<T> mappedClass)
    {
        initialize(mappedClass);
    }

    public void setMappedClass(Class<T> mappedClass)
    {
        initialize(mappedClass);
    }

    protected void initialize(Class<T> mappedClass)
    {
        this.mappedClass = mappedClass;
        IMetaDataProvider metaDataProvider = new TEMetaDataProvider();
        metaData = metaDataProvider.extractTableEntityMetaData(mappedClass);
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        String tmpColumn;
        for (PropertyDescriptor pd : pds)
        {
            if (pd.getWriteMethod() != null)
            {
                tmpColumn = metaData.getColumnName(pd.getName());
                if (tmpColumn != null)
                    this.mappedFields.put(tmpColumn, pd);
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        T mappedObject = BeanUtils.instantiate(this.mappedClass);
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int index = 1; index <= columnCount; index++)
        {
            String column = JdbcUtils.lookupColumnName(rsmd, index).toUpperCase();// 获取列名
            PropertyDescriptor pd = this.mappedFields.get(column);// 获取列对应的属性
            if (pd != null)
            {
                Object value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
                bw.setPropertyValue(pd.getName(), value);
            }
        }
        return mappedObject;
    }

    public static <T> AnnonBeanPropertyRowMapper<T> newInstance(Class<T> mappedClass)
    {
        AnnonBeanPropertyRowMapper<T> newInstance = new AnnonBeanPropertyRowMapper<T>();
        newInstance.setMappedClass(mappedClass);
        return newInstance;
    }
}
