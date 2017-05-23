package cn.huaxunchina.common.jdbcsupport;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

public class SimpleSqlBuilder
{
    public final static String SQL_TPL_SAVE = "INSERT INTO {0}({1}) VALUES({2})";
    public final static String SQL_TPL_UPDATE = "UPDATE {0} SET {1} where {2}=:{3}";
    public final static String SQL_TPL_QERUY_BY_ACOLUMN = "select * from {0}";
    public final static String SQL_TPL_DEL_BY_ACOLUMN = "delete from {0} where {1}=?";
    /**
     * 分页SQL
     */
    //public static final String MYSQL_PAGESQL = "select * from ( {0}) stab00 limit {1},{2}";  
    //public static final String POSTGRE_PAGESQL = "select * from ( {0}) stab00 limit {2} offset {1}";
    public static final String ORACLE_PAGESQL = "select * from (select querycol.*,rownum rownum_ from ({0}) querycol where rownum <= {1}) where rownum_>{2}";
    public static final String SQLSERVER_PAGESQL = "select * from ( select row_number() over(order by tempColumn) tempRowNumber, * from (select top {1} tempColumn = 0, {0}) t ) tt where tempRowNumber > {2}";
    
    public enum DbType{Oracle,PostgreSQL,MySql,SqlServer}

    /**
     * 生成查询所有列的select语句
     * 
     * @param metaData
     * @param where 附带查询条件
     * @param orderBy 排序方式 .如:id asc
     * @return
     */
    public static String selectSql(TableEntityMetaData metaData, String where, String orderBy)
    {
        String selectSQL = MessageFormat.format(SQL_TPL_QERUY_BY_ACOLUMN, metaData.getTableName());
        if (where != null && !where.isEmpty())
            selectSQL = selectSQL + " where " + where;
        if (orderBy != null && !orderBy.isEmpty())
            selectSQL = selectSQL + " order by " + orderBy;
        return selectSQL;
    }

    /**
     * 生成insert命名参数sql
     * 
     * @param metaData
     * @return
     */
    public static String insertSql(TableEntityMetaData metaData, String[] excludeProperties)
    {
        Map<String, ColumnMetaData> cmMap = metaData.getColumnsMap();
        StringBuilder strbuf_cols = new StringBuilder(cmMap.size() * 6);
        StringBuilder strbuf_param = new StringBuilder(strbuf_cols.length());
        boolean haveExclude = excludeProperties != null && excludeProperties.length > 0;
        char[] splitChars = { ',', ':', '=' };
        for (Entry<String, ColumnMetaData> kv : metaData.getColumnsMap().entrySet())
        {
            // 主键为非手动赋值时省略
            if (kv.getValue().isPrimaryKey() && metaData.isKeyGenerated())
            {
                continue;
            }
            if (!kv.getValue().isInsertable())
            {
                continue;
            }
            if (haveExclude && ArrayUtils.contains(excludeProperties, kv.getKey()))
            {
                continue;
            }
            strbuf_cols.append(kv.getValue().getColumnName()).append(splitChars[0]);
            strbuf_param.append(splitChars[1]).append(kv.getKey()).append(splitChars[0]);
        }
        strbuf_cols.deleteCharAt(strbuf_cols.length() - 1);
        strbuf_param.deleteCharAt(strbuf_param.length() - 1);
        return MessageFormat.format(SQL_TPL_SAVE, metaData.getTableName(), strbuf_cols, strbuf_param);
    }

    /**
     * 生成update命名参数sql
     * 
     * @param metaData
     * @param propertyName 条件的属性名
     * @return
     */
    public static String updateSql(TableEntityMetaData metaData, String propertyName)
    {
        Map<String, ColumnMetaData> cmMap = metaData.getColumnsMap();
        StringBuilder strbuf_cols = new StringBuilder(cmMap.size() * 6);
        char[] splitChars = { ',', ':', '=' };
        for (Entry<String, ColumnMetaData> kv : metaData.getColumnsMap().entrySet())
        {
            if (kv.getValue().isPrimaryKey() || !kv.getValue().isUpdateable())
            {
                continue;
            }
            strbuf_cols.append(kv.getValue().getColumnName()).append(splitChars[2]);
            strbuf_cols.append(splitChars[1]).append(kv.getKey()).append(splitChars[0]);
        }
        strbuf_cols.deleteCharAt(strbuf_cols.length() - 1);
        return MessageFormat.format(SQL_TPL_UPDATE, metaData.getTableName(), strbuf_cols, metaData.getColumnName(propertyName), propertyName);
    }

    /**
     * 生成update命名参数sql，指定更新属性名，即使该列有注解isupdateable=true仍更新
     * 
     * @param metaData
     * @param wherePropertyName 条件属性名
     * @param includeProperties 要更新的属性名
     * @return
     */
    public static String updateSql(TableEntityMetaData metaData, String wherePropertyName, String[] includeProperties)
    {
        Map<String, ColumnMetaData> cmMap = metaData.getColumnsMap();
        StringBuilder strbuf_cols = new StringBuilder(cmMap.size() * 6);
        char[] splitChars = { ',', ':', '=' };
        for (Entry<String, ColumnMetaData> kv : metaData.getColumnsMap().entrySet())
        {
            if (kv.getValue().isPrimaryKey() || !kv.getValue().isUpdateable()
                    || !ArrayUtils.contains(includeProperties, kv.getKey()))
            {
                continue;
            }
            strbuf_cols.append(kv.getValue().getColumnName()).append(splitChars[2]);
            strbuf_cols.append(splitChars[1]).append(kv.getKey()).append(splitChars[0]);
        }
        strbuf_cols.deleteCharAt(strbuf_cols.length() - 1);
        return MessageFormat.format(SQL_TPL_UPDATE, metaData.getTableName(), strbuf_cols, metaData.getColumnName(wherePropertyName), wherePropertyName);
    }

    /**
     * 生成update命名参数sql，指定不需要更新的列，注解isupdateable=false同生效
     * 
     * @param metaData
     * @param wherePropertyName
     * @param excludeProperties
     * @return
     */
    public static String updateWithExcludePropSql(TableEntityMetaData metaData, String wherePropertyName, String[] excludeProperties)
    {
        Map<String, ColumnMetaData> cmMap = metaData.getColumnsMap();
        StringBuilder strbuf_cols = new StringBuilder(cmMap.size() * 6);
        char[] splitChars = { ',', ':', '=' };
        for (Entry<String, ColumnMetaData> kv : metaData.getColumnsMap().entrySet())
        {
            if (kv.getValue().isPrimaryKey() || !kv.getValue().isUpdateable()
                    || ArrayUtils.contains(excludeProperties, kv.getKey()))
            {
                continue;
            }
            strbuf_cols.append(kv.getValue().getColumnName()).append(splitChars[2]);
            strbuf_cols.append(splitChars[1]).append(kv.getKey()).append(splitChars[0]);
        }
        strbuf_cols.deleteCharAt(strbuf_cols.length() - 1);
        return MessageFormat.format(SQL_TPL_UPDATE, metaData.getTableName(), strbuf_cols, metaData.getColumnName(wherePropertyName), wherePropertyName);
    }

    /**
     * 生成按列删除的语句
     * 
     * @param metaData
     * @param colunmName
     * @return
     */
    public static String deleteSql(TableEntityMetaData metaData, String colunmName)
    {
        return MessageFormat.format(SQL_TPL_DEL_BY_ACOLUMN, metaData.getTableName(), colunmName);
    }
    
    /**
     * 根据数据库类型生成分页语句
     * 
     * @param sql
     * @param dbType 数据库类型,只支持mysql,oracle,sqlserver,postgresql
     * @return
     */
    public static String pageSql(String sql,int startIndex,int pageSize,DbType dbType)
    {
        String pageSql;
        switch (dbType)
        {
        case MySql:
            pageSql=sql+" limit " + startIndex + ","+ pageSize;
            break;
        case PostgreSQL:
            pageSql=sql+" limit " + pageSize + ",offset"+ startIndex;
            break;
        case Oracle:
            pageSql=MessageFormat.format(ORACLE_PAGESQL, sql,startIndex+pageSize,startIndex);
            break;
        case SqlServer:
            int selectIndex = sql.toLowerCase().indexOf("select");
            int selectDistinctIndex = sql.toLowerCase().indexOf("select distinct");
            pageSql=sql.substring(selectIndex + (selectDistinctIndex == selectIndex ? 15 : 6));
            pageSql=MessageFormat.format(SQLSERVER_PAGESQL, pageSql,startIndex+pageSize,startIndex);
        default:
            pageSql=sql;
            break;
        }
        return pageSql;
    }

}
