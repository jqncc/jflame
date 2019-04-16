package org.jflame.toolkit.test.entity;

import java.io.Serializable;
import java.util.Date;

import org.jflame.toolkit.excel.ExcelColumn;
import org.jflame.toolkit.excel.IExcelEntity;

public class Pet implements IExcelEntity, Serializable {

    private String name;
    private int age;
    private String skin;
    private Date birthday;
    private double money;
    private Date createDate;

    public Pet() {
    }

    public Pet(String name) {
        this.name = name;
    }

    public Pet(String name, int age, String skin, Date birthday, double money) {
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
    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @ExcelColumn(name = "创建时间", order = 6)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
        builder.append("money=")
                .append(money)
                .append(", ");
        if (createDate != null) {
            builder.append("createDate=")
                    .append(createDate);
        }
        builder.append("]");
        return builder.toString();
    }

}
