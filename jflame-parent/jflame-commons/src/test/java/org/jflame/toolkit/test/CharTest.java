package org.jflame.toolkit.test;

import org.junit.Test;

import org.jflame.commons.util.ChineseHelper;

public class CharTest {

    @Test
    public void testChineseHelper() {
        System.out.println("获取拼音首字母：" + ChineseHelper.getLetterForshort("大中国南昌中大china"));
        System.out.println(ChineseHelper.randomChinese(4));
        System.out.println("转拼音带声调 :" + ChineseHelper.convertToPinyin("中华人民共和国"));
        System.out.println("转拼音 不带声调:" + ChineseHelper.convertToPinyinNoTone("中华人民共和国"));
        System.out.println("简转繁体: 中华人民共和国繁体写法=" + ChineseHelper.simplifiedChineseToBig5("中华人民共和国是全人类最伟大的国家"));
        System.out.println("繁体转简体: 中華人民共和國=" + ChineseHelper.big5ToSimplifiedChinese("中華人民共和國"));
    }

}
