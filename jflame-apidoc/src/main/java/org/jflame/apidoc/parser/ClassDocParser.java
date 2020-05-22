package org.jflame.apidoc.parser;

import java.util.List;
import java.util.Optional;

import org.jflame.apidoc.model.ApiMethod;
import org.jflame.apidoc.util.JavadocUtils;
import org.jflame.apidoc.util.StringUtils;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;

public abstract class ClassDocParser {

    /**
     * 解析一个class文件中的接口文档描述
     * 
     * @param cls ClassDoc,类的文档描述
     * @return
     */
    public abstract List<ApiMethod> parse(ClassDoc cls);

    /**
     * 从api方法注释中提取api名称.
     * <p>
     * 优先取注释字符串中首个.号前面的内容,标题长度不大于ApiMethod.API_NAME_MAX_LEN
     * 
     * @param commentText
     * @return
     * @see ApiMethod.API_NAME_MAX_LEN
     */
    protected String extractApiName(String commentText) {
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
    protected boolean isIgnoreDoc(ProgramElementDoc doc) {
        return JavadocUtils.hasAnyAnnotation(doc, annot_deprecated) || JavadocUtils.hasTag(doc, TagConsts.ignore);
    }

    /**
     * 判断是否必填,有BeanValidation的非空注解即为必填"NotNull", "NotBlank", "NotEmpty"
     * 
     * @param fieldDoc
     * @return 必填时返回true
     */
    protected boolean isNotNullParam(ProgramElementDoc fieldDoc) {
        return JavadocUtils.hasAnyAnnotation(fieldDoc, "NotNull", "NotBlank", "NotEmpty");
    }

    /**
     * 判断是否忽略类成员变量.
     * <p>
     * 判断条件:<br>
     * 注释带@ignore;<br>
     * 带json忽略注解;<br>
     * 静态变量忽略;<br>
     * 反序列化时不带setter方法;<br>
     * 序列化不带getter方法
     * </p>
     * 
     * @param clsDoc
     * @param fieldDoc
     * @param isSerial true=序列化
     * @return
     */
    protected boolean isIgnoreField(ClassDoc clsDoc, FieldDoc fieldDoc, boolean isSerial) {
        if (isIgnoreDoc(fieldDoc) || isIgnoreInJson(fieldDoc, false) || fieldDoc.isStatic()) {
            return true;
        }
        String getter = "get" + StringUtils.firstLetterUpperCase(fieldDoc.name());
        Optional<MethodDoc> mthDoc = JavadocUtils.getMethodDoc(clsDoc, getter, true);
        if (mthDoc.isPresent()) {
            // 如果getter方法带@ignore注释或json忽略注解
            if (isIgnoreDoc(mthDoc.get()) || isIgnoreInJson(mthDoc.get(), isSerial)) {
                return true;
            }
        }
        if (isSerial) {
            // 序列化时,存在正常的getter方法,且方法上没加忽略注解
            return !mthDoc.isPresent() || "void".equals(mthDoc.get().returnType().typeName());
        } else {
            // 反序列化带setter方法
            String setter = "set" + StringUtils.firstLetterUpperCase(fieldDoc.name());
            Optional<MethodDoc> setMthDoc = JavadocUtils.getMethodDoc(clsDoc, setter, false);
            return !setMthDoc.isPresent() || !"void".equals(setMthDoc.get().returnType().typeName());
        }
    }

    static String[] jsonAnnots = { "JsonIgnore","Expose","JsonField" };

    /**
     * 判断json序列化是否忽略,解析json组件的忽略注解
     * 
     * @param doc doc
     * @param isSerial true=序列化,false=反序列化
     * @return
     */
    public static boolean isIgnoreInJson(ProgramElementDoc doc, boolean isSerial) {
        AnnotationDesc[] annotDescs = doc.annotations();
        if (annotDescs.length > 0) {
            for (AnnotationDesc desc : annotDescs) {
                // jackson @JsonIgnore注解
                if (jsonAnnots[0].equals(desc.annotationType().name())) {
                    return true;
                }
                // gson @Expose注解,fastjson @JsonField注解
                if (jsonAnnots[1].equals(desc.annotationType().name())
                        || jsonAnnots[2].equals(desc.annotationType().name())) {
                    if (isSerial) {
                        Optional<Object> value = JavadocUtils.getAnnotationProperty(desc, "serialize");
                        if (value.isPresent()) {
                            if (!(boolean) value.get()) {
                                return true;
                            }
                        }
                    } else {
                        Optional<Object> value = JavadocUtils.getAnnotationProperty(desc, "deserialize");
                        if (value.isPresent()) {
                            if (!(boolean) value.get()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
