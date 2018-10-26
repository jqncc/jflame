package org.jflame.toolkit.test.entity;

import org.jflame.toolkit.excel.ExcelColumn;

public class Cat extends Pet {

    @ExcelColumn(name = "花纹", order = 7)
    private String streak;
    @ExcelColumn(name = "重量", order = 8)
    private float weight;

    public String getStreak() {
        return streak;
    }

    public void setStreak(String streak) {
        this.streak = streak;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

}
