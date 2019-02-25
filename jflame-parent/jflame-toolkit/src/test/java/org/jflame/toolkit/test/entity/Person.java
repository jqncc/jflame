package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.jflame.toolkit.test.Payserver.Adult;
import org.jflame.toolkit.test.Payserver.Child;
import org.jflame.toolkit.valid.Digit;
import org.jflame.toolkit.valid.DynamicValid;
import org.jflame.toolkit.valid.DynamicValid.ValidRule;

public class Person implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @NotBlank
    @DynamicValid(rules = ValidRule.letter, message = "名字不正确")
    private String name;
    @Min(payload = Child.class, value = 10)
    @Max(payload = Adult.class, value = 30)
    private int age;
    private String sex;
    private int height;
    @Digit(maxScale = 2, minScale = 1, max = "500", min = "1", message = "体重不对")
    private BigDecimal weight;

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

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

}
