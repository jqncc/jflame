package org.jflame.toolkit.test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jflame.toolkit.reflect.BeanHelper;
import org.jflame.toolkit.test.entity.Cat;
import org.jflame.toolkit.test.entity.Pig;
import org.junit.Test;

public class ReflectTest {

    @Test
    public void test() {
        PropertyDescriptor[] props = BeanHelper.getPropertyDescriptors(Pig.class);
        for (PropertyDescriptor prop : props) {
            System.out.println(prop.getName());
        }
    }

    @Test
    public void testField() {
        for (Field field : Cat.class.getDeclaredFields()) {
            System.out.println(field.getName());
        }
        // Field field = FieldUtils.getDeclaredField(Cat.class, "streak");
        Field field = FieldUtils.getField(Cat.class, "streak", true);
        System.out.println(field.getName());
    }
}
