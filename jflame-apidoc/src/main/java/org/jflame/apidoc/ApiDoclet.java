package org.jflame.apidoc;

import java.util.List;

import org.jflame.apidoc.model.ApiMethod;
import org.jflame.apidoc.model.ApiProject;
import org.jflame.apidoc.parser.SpringMvcDocParser;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

/**
 * @author yucan.zhang
 */
public class ApiDoclet extends Doclet {

    public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();

        ApiProject proj = new ApiProject();
        proj.setBaseUrl("https://localhost");
        proj.setDesc("工程局大项目");
        proj.setProjName("全球统治系统");
        proj.setVersion(1);
        SpringMvcDocParser docParser = new SpringMvcDocParser();
        for (ClassDoc cls : classes) {
            List<ApiMethod> methods = docParser.parse(cls);
            System.out.println(methods);
            /*
             * AnnotationDesc[] adescs = cls.annotations(); if (ArrayUtils.isNotEmpty(adescs)) { for (AnnotationDesc
             * adesc : adescs) { System.out.println(adesc.annotationType().name()); if
             * (ArrayUtils.isNotEmpty(adesc.elementValues())) { for (ElementValuePair elePair : adesc.elementValues()) {
             * System.out.println(elePair.element().name()); if (elePair.value().value() instanceof AnnotationValue[]) {
             * System.out.println(((AnnotationValue[]) elePair.value().value())[0]); } else {
             * System.out.println(elePair.value().value().toString()); } } } } }
             */
            /* System.out.println(cls.qualifiedName() + ":" + cls.isOrdinaryClass());
            // System.out.println(JavadocUtils.hasAnyAnnotation(cls, "Contoller"));
            // System.out.println(cls.qualifiedName());
            FieldDoc[] fields = cls.fields(true);
            for (FieldDoc flc : fields) {
                if (StringUtils.isEmpty(flc.commentText()) || flc.isTransient()) {
                    continue;
                }
            }
            
            MethodDoc[] methods = cls.methods(true); // 获取包含私有方法在内的所有方法
            for (MethodDoc meth : methods) {
                System.out.println("meth:" + meth.qualifiedName() + " returnType:" + meth.returnType());
                if (meth.paramTags().length > 0) {
                    System.out.println("paramTag==============");
                    System.out.println(meth.paramTags()[0].parameterName());
                    System.out.println(meth.paramTags()[0].parameterComment());
            
                }
            
                if (meth.parameters().length > 0) {
                    System.out.println("param----");
                    System.out.println(meth.parameters()[0].name());
                    System.out.println(meth.parameters()[0].typeName());
                    System.out.println(meth.parameters()[0].type().qualifiedTypeName());
                }
            
                if (meth.typeParameters().length > 0) {
                    System.out.println("typeParam----");
                    System.out.println(meth.typeParameters()[0].qualifiedTypeName());
                }
            }*/
        }

        return true;
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
}
