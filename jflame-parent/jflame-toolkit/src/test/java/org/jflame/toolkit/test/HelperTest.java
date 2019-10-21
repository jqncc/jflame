package org.jflame.toolkit.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.jflame.toolkit.test.entity.Pig;
import org.jflame.toolkit.util.ArrayHelper;
import org.jflame.toolkit.util.CollectionHelper;

public class HelperTest {

    @Test
    public void testCollectionHelper() {
        Pig smallPig = new Pig(1, "smallPig", 20, "white");
        Pig midPig = new Pig(1, "midPig", 100, "black");
        List<Pig> lst = CollectionHelper.newList(smallPig, midPig);
        String[] pigNames = ArrayHelper.toArray(lst, l -> l.getName());
        System.out.println(Arrays.toString(pigNames));
    }

}
