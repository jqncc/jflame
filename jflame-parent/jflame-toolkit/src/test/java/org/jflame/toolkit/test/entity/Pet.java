package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.jflame.commons.excel.ExcelColumn;
import org.jflame.commons.excel.IExcelEntity;

public class Pet implements IExcelEntity, Serializable {

    private static final long serialVersionUID = -3318368228016050296L;
    private String name;
    private int age;
    @ExcelColumn(name = "体重", order = 7)
    private double weight;
    private String skin;
    private Date birthday;
    private BigDecimal money;
    @ExcelColumn(name = "创建时间", order = 8, fmt = "yyyy年MM月dd日 HH:mm:ss")
    private LocalDateTime createDate;
    @ExcelColumn(name = "防疫是否合格", order = 6)
    private Boolean hasCert;

    public Pet() {
    }

    public Pet(String name) {
        this.name = name;
    }

    public Pet(String name, int age, String skin, Date birthday, BigDecimal money) {
        super();
        this.name = name;
        this.age = age;
        this.skin = skin;
        this.birthday = birthday;
        this.money = money;
    }

    @ExcelColumn(name = "名称", order = 1)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelColumn(name = "年龄", order = 2)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @ExcelColumn(name = "皮肤", order = 3)
    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    @ExcelColumn(name = "生日", order = 4, fmt = "yyyy/MM/dd")
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @ExcelColumn(name = "价格", order = 5)
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Boolean getHasCert() {
        return hasCert;
    }

    public void setHasCert(Boolean hasCert) {
        this.hasCert = hasCert;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Pet [");
        if (name != null) {
            builder.append("name=")
                    .append(name)
                    .append(", ");
        }
        builder.append("age=")
                .append(age)
                .append(", weight=")
                .append(weight)
                .append(", ");
        if (skin != null) {
            builder.append("skin=")
                    .append(skin)
                    .append(", ");
        }
        if (birthday != null) {
            builder.append("birthday=")
                    .append(birthday)
                    .append(", ");
        }
        if (money != null) {
            builder.append("money=")
                    .append(money)
                    .append(", ");
        }
        if (createDate != null) {
            builder.append("createDate=")
                    .append(createDate)
                    .append(", ");
        }
        if (hasCert != null) {
            builder.append("hasCert=")
                    .append(hasCert);
        }
        builder.append("]");
        return builder.toString();
    }

}
