package org.jflame.web.tag;

import java.util.ArrayList;
import java.util.List;

import org.jflame.toolkit.common.bean.NameValuePair;
import org.jflame.toolkit.util.CollectionHelper;
/**
 * 生成select控件，数据源来自字典表
 * @author zyc
 */
@SuppressWarnings("serial")
public class UISelectDictTag extends AbstractUISelectTag
{
    private String type;//字典类型

    @Override
    protected List<NameValuePair> getBindData()
    {
      /*  ApplicationContext ctx= WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        IDictService service=(IDictService)ctx.getBean("dictServiceImpl");
        List<NameValuePair> dataSource=null;
        if(service!=null)
        {
            List<DictItem> dicts=service.getDictItemsByType(type);
            if(!CollectionHelper.isNullOrEmpty(dicts))
            {
                dataSource=new ArrayList<>(dicts.size());
                for (DictItem dictItem : dicts)
                {
                    dataSource.add(new NameValuePair(dictItem.getItemName(),dictItem.getItemValue()));
                }
            }
        }*/
        return null;
    }
    
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
