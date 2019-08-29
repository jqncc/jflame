package org.jflame.toolkit.test.entity;

import org.jflame.toolkit.excel.ExcelColumn;

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

        builder.append(super.toString());

        builder.append("]");
        return builder.toString();
    }

}
