package org.jflame.toolkit.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.MapHelper;
import org.jflame.toolkit.test.entity.Cat;

public class CollectTest {

    @Test
    public void testCollect() {
        List<String> l1 = CollectionHelper.newList("a", "b", "c");
        List<String> l2 = CollectionHelper.newList("a", "b", "c");
        List<String> l3 = CollectionHelper.newList("b", "c", "a");
        List<String> l4 = CollectionHelper.newList("a", "c", "b", "d");
        List<String> l5 = CollectionHelper.newList("a", "c");
        System.out.println(CollectionHelper.elementEquals(l1, l2));
        System.out.println(CollectionHelper.elementEquals(l1, l3));
        System.out.println(CollectionHelper.elementEquals(l1, l4));
        System.out.println(CollectionHelper.elementEquals(l1, l5));

        File f = new File("C:\\Users\\yucan.zhang\\Desktop\\jieba词性.png");
        System.out.println(f.getPath());
    }

    @Test
    public void testMapHelper() {
        Cat c1 = new Cat();
        c1.setAge(1);
        c1.setName("redcat");
        c1.setSkin("red");

        Cat c2 = new Cat();
        c2.setAge(2);
        c2.setName("blackcat");
        c2.setSkin("black");

        List<Cat> lst = CollectionHelper.newList(c1, c2);
        Map<String,Cat> map = MapHelper.toMap(lst, cat -> cat.getName());
        System.out.println(map);
    }
}
