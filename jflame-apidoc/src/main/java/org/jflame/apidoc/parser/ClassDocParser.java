package org.jflame.apidoc.parser;

import java.util.List;

import org.jflame.apidoc.model.ApiMethod;
import org.jflame.apidoc.util.JavadocUtils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;

public interface ClassDocParser {

    /**
     * 解析一个class文件中的接口文档描述
     * 
     * @param cls ClassDoc,类的文档描述
     * @return
     */
    public List<ApiMethod> parse(ClassDoc cls);

    /**
     * 从api方法注释中提取api名称.
     * <p>
     * 优先取注释字符串中首个.号前面的内容,标题长度不大于ApiMethod.API_NAME_MAX_LEN
     * 
     * @param commentText
     * @return
     * @see ApiMethod.API_NAME_MAX_LEN
     */
    public default String extractApiName(String commentText) {
        int firstDotIndex = commentText.indexOf('.');
        String apiName;
        if (firstDotIndex >= 0) {
            apiName = commentText.substring(0, firstDotIndex);
        } else {
            apiName = commentText;
        }
        if (apiName.length() > ApiMethod.API_NAME_MAX_LEN) {
            apiName = apiName.substring(0, ApiMethod.API_NAME_MAX_LEN - 1);
        }
        return apiName;
    }

    String annot_deprecated = "Deprecated";// @Deprecated废除注解

    /**
     * 是否忽略提取方法doc,方法注解了@Deprecated或注释使用了@ignore将忽略
     * 
     * @param doc
     * @return
     */
    public default boolean isIgnoreDoc(MethodDoc doc) {
        return JavadocUtils.hasAnyAnnotation(doc, annot_deprecated) || JavadocUtils.hasTag(doc, TagConsts.ignore);
    }
}
