package cn.huaxunchina.common.datasouce;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源切换
 * 
 * @author zyc
 */
public class DynamicDataSource extends AbstractRoutingDataSource
{
    
    @Override
    protected Object determineCurrentLookupKey()
    {
        return DynamicDataSourceHolder.getDataSouce();
    }

}
