package org.jflame.db.datasource;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.aop.MethodBeforeAdvice;

/**
 * 用于数据源切换的aop通知.根据配置的方法名规则切换到对应的数据源
 * 
 * @author zyc
 */
public class DataSourceSwitchAdvice implements MethodBeforeAdvice
{
    private Map<String, String> dataSourceForMethod;

    public void setDataSourceForMethod(Map<String, String> dataSourceForMethod)
    {
        this.dataSourceForMethod = dataSourceForMethod;
    }

    @Override
    public void before(Method method, Object[] arg1, Object arg2) throws Throwable
    {
        if (dataSourceForMethod != null)
        {
            String methodName=method.getName();
            for (Entry<String, String> kv: dataSourceForMethod.entrySet())
            {
                if(kv.getKey().startsWith(methodName))
                {
                    DynamicDataSourceHolder.setDataSource(kv.getValue());
                }
            }
        }
    }

}
