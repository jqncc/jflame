package org.jflame.toolkit.test.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pigs")
public class Pig {

    private int age;
    private String name;
    private int weight;
    private String skin;

    public Pig() {
    }

    public Pig(int age, String name, int weight, String skin) {
        this.age = age;
        this.name = name;
        this.weight = weight;
        this.skin = skin;
    }

    @XmlElement
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @XmlElement
    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Pig [age=")
                .append(age)
                .append(", ");
        if (name != null) {
            builder.append("name=")
                    .append(name)
                    .append(", ");
        }
        builder.append("weight=")
                .append(weight)
                .append(", ");
        if (skin != null) {
            builder.append("skin=")
                    .append(skin);
        }
        builder.append("]");
        return builder.toString();
    }

}
