package org.jflame.mvc.support;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.jflame.toolkit.util.DateHelper;
import org.springframework.util.StringUtils;

/**
 * 多日期格式转换器<p>
 * 默认支持格式："yyyy-MM-dd","yyyy-MM-dd HH:mm:ss","yyyy年MM月dd日","yyyyMMddHHmmss"
 * 
 * @author yucan.zhang
 *
 */
public class MyCustomDateEditor extends PropertyEditorSupport {
	
	private final DateFormat defaultDateFormat;
	
	public MyCustomDateEditor(){
		defaultDateFormat=null;
	}
	
	/**
	 * 构造函数，指定特定格式DateFormat
	 * @param dateFormat
	 */
	public MyCustomDateEditor(DateFormat dateFormat) {
		this.defaultDateFormat = dateFormat;
	}
	
	/**
	 * Parse the Date from the given text, using the specified DateFormat.
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtils.hasText(text)) {
			setValue(null);
		}
		else {
			try {
				//未指定固定格式器，使用所有支持格式处理
				if(defaultDateFormat==null){
					setValue(DateHelper.parseDate(text, DateHelper.formats));
				}else{
					setValue(this.defaultDateFormat.parse(text));
				}
			}
			catch (ParseException ex) {
				throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Format the Date as String, using the specified DateFormat.
	 */
	@Override
	public String getAsText() {
		Date value = (Date) getValue();
		String dateStr = "";
		if (value != null) {
			if (defaultDateFormat == null) {
				if (value instanceof java.sql.Date) {
					dateStr = DateHelper.format(value, DateHelper.CN_YYYY_MM_DD);
				} else {
					dateStr = DateHelper.format(value, DateHelper.YYYY_MM_DD_HH_mm_ss);
				}
			} else {
				dateStr = this.defaultDateFormat.format(value);
			}
		}
		return dateStr;
	}
}
