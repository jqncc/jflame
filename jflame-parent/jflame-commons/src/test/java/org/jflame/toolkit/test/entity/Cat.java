package org.jflame.toolkit.test.entity;

import org.jflame.commons.excel.ExcelColumn;

public class Cat extends Pet {

    private static final long serialVersionUID = 6485997342160820822L;
    @ExcelColumn(name = "花纹", order = 9)
    private String streak;

    public String getStreak() {
        return streak;
    }

    public void setStreak(String streak) {
        this.streak = streak;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cat [");
        if (streak != null) {
            builder.append("streak=")
                    .append(streak)
                    .append(", ");
        }
        if (getName() != null) {
            builder.append("name=")
                    .append(getName())
                    .append(", ");
        }
        builder.append("age=")
                .append(getAge())
                .append(", ");
        if (getSkin() != null) {
            builder.append("skin=")
                    .append(getSkin())
                    .append(", ");
        }
        if (getBirthday() != null) {
            builder.append("birthday=")
                    .append(getBirthday())
                    .append(", ");
        }
        if (getMoney() != null) {
            builder.append("money=")
                    .append(getMoney())
                    .append(", ");
        }
        if (getCreateDate() != null) {
            builder.append("createDate=")
                    .append(getCreateDate())
                    .append(", ");
        }
        builder.append("weight=")
                .append(getWeight())
                .append(", ");
        if (getHasCert() != null) {
            builder.append("hasCert=")
                    .append(getHasCert());
        }
        builder.append("]");
        return builder.toString();
    }

}
