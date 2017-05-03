package org.jflame.toolkit.test;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.jflame.toolkit.test.Payserver.Adult;
import org.jflame.toolkit.test.Payserver.Child;
import org.jflame.toolkit.valid.DynamicValid;
import org.jflame.toolkit.valid.DynamicValidator.ValidRule;


public class Person implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @NotBlank
    @DynamicValid(rules=ValidRule.letter,message="名字不正确")
    private String name;
    @Min(payload=Child.class,value=10)
    @Max(payload=Adult.class,value=30)
    private int age;
    private String sex;
    private int height;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    
}
