package org.jflame.db.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据库表元数据
 * 
 * @author yucan.zhang
 */
public class TableMetaData {

    private String tableName;// 表名
    private String entityName;// 实体类名
    private IdMetaData key;
    // private String keyColumn;// 主键列名
    // private String keyProperty;// 主键属性名
    // private boolean keyGenerated=false;//主键生成策略,是否是手动赋值的

    private Map<String,ColumnMetaData> columnsMap = new LinkedHashMap<>();// 属性名->列元数据

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /*
     * public String getKeyColumn() { return keyColumn; } public void setKeyColumn(String keyColumn) { this.keyColumn =
     * keyColumn; } public String getKeyProperty() { return keyProperty; } public void setKeyProperty(String
     * keyProperty) { this.keyProperty = keyProperty; } public boolean isKeyGenerated() { return keyGenerated; } public
     * void setKeyGenerated(boolean keyGenerated) { this.keyGenerated = keyGenerated; }
     */

    /**
     * 获取内部属性名与列名的map
     * 
     * @return
     */
    public Map<String,ColumnMetaData> getColumnsMap() {
        return columnsMap;
    }

    /**
     * 增加一对属性与列名
     * 
     * @param propertyName 属性名
     * @param columnName 列名
     */
    public void addProperty(String propertyName, ColumnMetaData column) {
        columnsMap.put(propertyName, column);
    }

    /**
     * 根据属性名返回对应的列名
     * 
     * @param propertyName 属性名
     * @return
     */
    public String getColumnName(String propertyName) {
        ColumnMetaData column = columnsMap.get(propertyName);
        if (column != null)
            return column.getColumnName();
        return null;
    }

    /**
     * 根据列名返回对应的属性名
     * 
     * @param columnName 列名
     * @return
     */
    public String getPropertyName(String columnName) {
        for (Entry<String,ColumnMetaData> kv : columnsMap.entrySet()) {
            if (kv.getValue().getColumnName().equals(columnName))
                return kv.getKey();
        }
        return null;
    }

    /**
     * 取得属性名与列名map
     * 
     * @return
     */
    public Map<String,String> getNameMap() {
        Map<String,String> nameMap = new HashMap<>();
        for (Entry<String,ColumnMetaData> kv : columnsMap.entrySet()) {
            nameMap.put(kv.getKey(), kv.getValue().getColumnName());
        }
        return nameMap;
    }

    /**
     * 返回所有列名
     * 
     * @param isIncludedKey是否包含主键列
     * @return
     */
    public String[] getColumnNames(boolean isIncludedKey) {
        Collection<ColumnMetaData> cols = columnsMap.values();
        boolean flag = !isIncludedKey && key.getColumnName() != null;// 不包含主键
        String[] colNames;
        if (flag) {
            colNames = new String[cols.size() - 1];
        } else {
            colNames = new String[cols.size()];
        }
        int i = 0;
        Iterator<ColumnMetaData> it = cols.iterator();
        while (it.hasNext()) {
            ColumnMetaData cmd = it.next();
            if (!flag || !cmd.isPrimaryKey()) {
                colNames[i] = cmd.getColumnName();
                i++;
            }
        }
        return colNames;
    }

    public String[] getPropertyNames(boolean isIncludedKey) {
        Collection<String> cols = columnsMap.keySet();
        if (!isIncludedKey && key.getPropertyName() != null) {
            cols.remove(key.getPropertyName());
        }
        return cols.toArray(new String[cols.size()]);
    }

    public IdMetaData getKey() {
        return key;
    }

    public void setKey(IdMetaData key) {
        this.key = key;
    }

}
