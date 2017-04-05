package org.jflame.web.tag;

import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.NameValuePair;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * select通用标签.
 * 数据源data定义:<br>1.java.util.Map; 2.List&lt;NameValuePair&gt;; 3.1,2类型的json字符串或k1=v1&k2=v2
 * 
 * @author zyc
 */
public class UISelectTag extends AbstractUISelectTag
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected List<NameValuePair> getBindData()
    {
        List<NameValuePair> results = null;
        if (data != null)
        {
            if (data instanceof Map)
            {
                try
                {
                    results = NameValuePair.toList((Map<String, String>) data);
                } catch (ClassCastException e)
                {
                    results = null;
                    e.printStackTrace();
                }
            } else if (data instanceof List<?>)
            {
                try
                {
                    results = (List<NameValuePair>) data;
                } catch (ClassCastException e)
                {
                    results = null;
                    e.printStackTrace();
                }
            }
            else if (data instanceof String)
            {
                String dataStr = (String) data;
                char firstChar = dataStr.charAt(0);
                if (firstChar == '[')
                {
                    results = (List<NameValuePair>) JsonHelper.parseArray(dataStr, NameValuePair.class);
                } else if (firstChar == '{')
                {
                    Map<String, String> map =JsonHelper.parseMap(dataStr, String.class, String.class);
                    if (map != null)
                    {
                        results = NameValuePair.toList(map);
                    }
                } else if (dataStr.indexOf('=') >= 0)
                {
                    Map<String, String> map = StringHelper.buildMapFromUrlParam(dataStr);
                    if (map != null)
                    {
                        results = NameValuePair.toList(map);
                    }
                }
            }
        }
        return results;
    }

}
