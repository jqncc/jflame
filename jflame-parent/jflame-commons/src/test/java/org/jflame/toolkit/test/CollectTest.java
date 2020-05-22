package org.jflame.toolkit.test;

import java.io.File;
import java.util.List;

import org.junit.Test;

import org.jflame.commons.util.CollectionHelper;

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

}
