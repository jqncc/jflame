package org.jflame.toolkit.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelCreator;
import org.jflame.toolkit.excel.ExcelImportor;
import org.jflame.toolkit.test.entity.Cat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ExcelTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ExcelTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ExcelTest.class);
    }

    /**
     * 实体类导出到文件流
     */
    @org.junit.Test
    public void testExport() {
        List<Cat> pets = new ArrayList<>(300);
        Cat p = null;
        for (int i = 0; i < 2000; i++) {
            p = new Cat();
            p.setAge(i);
            p.setBirthday(new Date());
            p.setMoney(i * 2.3);
            p.setName("猫咪名" + i);
            p.setStreak(i % 2 == 0 ? "灰白相间" : "纯白");
            p.setSkin("猫的皮肤");
            p.setWeight(5.4f * i);
            p.setCreateDate(new Date());
            pets.add(p);
        }
        File f = new File("D:\\datacenter\\2.xlxs");
        try (ExcelCreator creator = new ExcelCreator()) {
            creator.createSheet();
            creator.fillEntityData(pets);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            creator.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 快捷静态方法导出
        // try( FileOutputStream out =new FileOutputStream("e:\\1.xls")) {
        // ExcelCreator.export(pets, out);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    /**
     * map数据导出
     */
    public void testExportA() {
        List<LinkedHashMap<String,Object>> pets = new ArrayList<>(300);
        for (int i = 0; i < 300; i++) {
            LinkedHashMap<String,Object> m = new LinkedHashMap<String,Object>();
            m.put("age", 30 + i);
            m.put("name", "唯唯诺诺" + i);
            m.put("birthday", new Date());
            m.put("skin", "白皮肤");
            m.put("salary", new BigDecimal("322" + i));
            pets.add(m);
        }
        File f = new File("D:\\datacenter\\map.xls");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            ExcelCreator.export(pets, new String[]{ "age","name","birthday","skin","salary" }, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入数据转为实体对象
     * 
     * @throws IOException
     */
    @org.junit.Test
    public void testImport() throws IOException {
        try (ExcelImportor xlsImport = new ExcelImportor("D:\\datacenter\\2.xlsx")) {
            xlsImport.setStepValid(false);
            xlsImport.setStartRowIndex(1);
            try {
                LinkedHashSet<Cat> results = xlsImport.importSheet(Cat.class);
                for (Cat pet : results) {
                    System.out.println(pet.getName() + ",age:" + pet.getAge() + ",skin:" + pet.getSkin() + ",money:"
                            + pet.getMoney() + ",birthday:" + pet.getBirthday());
                }

            } catch (ExcelAccessException e) {
                e.printStackTrace();
                Map<Integer,String> xMap = xlsImport.getErrorMap();
                System.out.println(xMap.values().toString());
            }
        }
        // List<Integer> resultIndexs=xlsImport.getCurRowIndexs();
    }

}
