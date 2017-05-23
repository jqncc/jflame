package org.jflame.db.metadata;

/**
 * @author yucan.zhang
 *
 */
public class ColumnMetaData
{
    private String columnName;
    private boolean insertable=true;
    private boolean updateable=true;
    private boolean isPrimaryKey=false;

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public boolean isInsertable()
    {
        return insertable;
    }

    public void setInsertable(boolean insertable)
    {
        this.insertable = insertable;
    }

    public boolean isUpdateable()
    {
        return updateable;
    }

    public void setUpdateable(boolean updateable)
    {
        this.updateable = updateable;
    }

    public boolean isPrimaryKey()
    {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey)
    {
        this.isPrimaryKey = isPrimaryKey;
    }

}
