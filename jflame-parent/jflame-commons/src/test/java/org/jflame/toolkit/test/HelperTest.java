package org.jflame.toolkit.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.toolkit.test.entity.Pig;

public class HelperTest {

    @Test
    public void testCollectionHelper() {
        Pig smallPig = new Pig(1, "smallPig", 20, "white");
        Pig midPig = new Pig(1, "midPig", 100, "black");
        List<Pig> lst = CollectionHelper.newList(smallPig, midPig);
        String[] pigNames = ArrayHelper.toArray(lst, l -> l.getName());
        System.out.println(Arrays.toString(pigNames));

    }

    public static void main(String[] args) {
        /* System.out.println(DigestHelper.md5Hex(HelperTest.class.getResource("/")
                .getPath()));
        System.out.println(TranscodeHelper.encodeHexString(HelperTest.class.getResource("/")
                .getPath()));*/

        String lastCode = "001200300801";
        String prodCode = lastCode.substring(0, lastCode.length() - 2);
        String codeOrderNo = StringUtils.substring(lastCode, -2);// 取最后两顺序位
        String partyCode = prodCode + StringUtils.leftPad(String.valueOf((Integer.parseInt(codeOrderNo) + 1)), 2, '0');
        System.out.println(partyCode);

    }
}
