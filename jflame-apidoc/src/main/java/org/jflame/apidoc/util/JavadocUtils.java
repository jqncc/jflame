package org.jflame.apidoc.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.jflame.apidoc.parser.TagConsts;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;

public final class JavadocUtils {

    /**
     * 查找文档元素上的指定注解描述
     * 
     * @param doc 文档元素ProgramElementDoc
     * @param annotationName 注解名称
     * @return Optional&lt;AnnotationDesc&gt;
     */
    public static Optional<AnnotationDesc> getAnnotation(ProgramElementDoc doc, String annotationName) {
        return getAnnotation(doc.annotations(), annotationName);
    }

    /**
     * 获取方法参数的注解描述
     * 
     * @param parameter Parameter
     * @param annotationName
     * @return
     */
    public static Optional<AnnotationDesc> getAnnotation(Parameter parameter, String annotationName) {
        return getAnnotation(parameter.annotations(), annotationName);
    }

    public static Optional<AnnotationDesc> getAnnotation(AnnotationDesc[] annotDescs, String annotationName) {
        if (annotDescs.length > 0) {
            for (AnnotationDesc desc : annotDescs) {
                if (desc.annotationType().name().equals(annotationName)) {
                    return Optional.of(desc);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 判断文档元素上是否有指定注解
     * 
     * @param doc 文档元素ProgramElementDoc
     * @param annotationNames 要查找的注解名称
     * @return
     */
    public static boolean hasAnyAnnotation(ProgramElementDoc doc, String... annotationNames) {
        AnnotationDesc[] annotDescs = doc.annotations();
        if (annotDescs.length > 0) {
            for (AnnotationDesc desc : annotDescs) {
                if (ArrayUtils.contains(annotationNames, desc.annotationType().name())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据成员变量名获取成员的FieldDoc
     * 
     * @param cls
     * @param fieldName
     * @return
     */
    public static Optional<FieldDoc> getFieldDoc(ClassDoc cls, String fieldName) {
        FieldDoc[] fields = cls.fields(false);
        if (fields.length > 0) {
            return Arrays.stream(fields).filter(f -> f.name().equals(fieldName)).findFirst();
        }
        return Optional.empty();
    }

    /**
     * 根据方法名获取MethodDoc
     * 
     * @param cls
     * @param methodName
     * @param isPublic false返回所有方法
     * @return
     */
    public static Optional<MethodDoc> getMethodDoc(ClassDoc cls, String methodName, boolean isPublic) {
        MethodDoc[] fields = cls.methods(isPublic);
        if (fields.length > 0) {
            return Arrays.stream(fields).filter(f -> f.name().equals(methodName)).findFirst();
        }
        return Optional.empty();
    }

    /**
     * 获取注解某个属性的值
     * 
     * @param desc
     * @param propertyName 属性名
     * @return
     */
    public static Optional<Object> getAnnotationProperty(AnnotationDesc desc, String propertyName) {
        if (ArrayUtils.isNotEmpty(desc.elementValues())) {
            for (ElementValuePair elePair : desc.elementValues()) {
                if (Objects.equals(propertyName, elePair.element().name())) {
                    return Optional.ofNullable(elePair.value().value());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 判断javadoc中是否有指定的doc tag
     * 
     * @param doc
     * @param tagName
     * @return
     */
    public static boolean hasTag(Doc doc, String tagName) {
        Tag[] tags = doc.tags(tagName);
        return ArrayUtils.isNotEmpty(tags);
    }

    /**
     * 获取一个tag的文本,如果有多个相同tag只返回第一个
     * 
     * @param doc
     * @param tagName tag name
     * @return
     */
    public static Optional<String> getTagText(Doc doc, String tagName) {
        Tag[] tags = doc.tags(tagName);
        if (ArrayUtils.isNotEmpty(tags)) {
            /*System.out.println(tags[0].kind());
            System.out.println(tags[0].name());
            System.out.println(tags[0].holder().commentText());
            System.out.println(tags[0].kind());*/
            return Optional.ofNullable(tags[0].text());
        }
        return Optional.empty();
    }

    /**
     * 获取类或方法注释上的作者. tag:<code>@author</code>
     * 
     * @param doc
     * @return
     */
    public static Optional<String> getAuthor(Doc doc) {
        if (doc instanceof ClassDoc || doc instanceof MethodDoc) {
            return getTagText(doc, TagConsts.author);
        } else {
            throw new IllegalArgumentException("@author只存在于类或方法注释上");
        }
    }

    /**
     * 获取类或方法注释上的模块名称. tag:<code>@module</code>
     * 
     * @param doc ClassDoc
     * @return
     */
    public static Optional<String> getModuleName(Doc doc) {
        if (doc instanceof ClassDoc || doc instanceof MethodDoc) {
            return getTagText(doc, TagConsts.module);
        } else {
            throw new IllegalArgumentException("@module只存在于类或方法注释上");
        }
    }

    /**
     * 获取类注释上的版本号.tag:<code>@version</code>
     * 
     * @param doc ClassDoc
     * @return
     */
    public static Optional<String> getVersion(ClassDoc doc) {
        return getTagText(doc, TagConsts.version);
    }

    /**
     * 获取方法参数的注释
     * 
     * @param doc 方法注释doc
     * @param paramName 参数名
     * @return
     */
    public static Optional<String> getParamComment(MethodDoc doc, String paramName) {
        ParamTag[] tags = doc.paramTags();
        String paramComment = null;
        if (tags.length > 0) {
            for (ParamTag paramTag : tags) {
                if (paramTag.parameterName().equals(paramName)) {
                    paramComment = paramTag.parameterComment();
                }
            }
        }
        return Optional.ofNullable(paramComment);
    }

}
