package org.jflame.toolkit.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelCreator;
import org.jflame.toolkit.excel.ExcelImportor;
import org.jflame.toolkit.test.entity.Cat;
import org.jflame.toolkit.util.MapHelper;
import org.jflame.toolkit.util.MathHelper;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class ExcelTest extends TestCase {

    /**
     * 实体类导出到文件流
     * 
     * @throws IOException
     */
    @org.junit.Test
    public void testExport() throws IOException {
        long start = System.nanoTime();
        List<Cat> pets = new ArrayList<>(20000);
        Cat p = null;
        // 生成导出数据
        for (int i = 1; i < 20000; i++) {
            p = new Cat();
            p.setAge(i);
            p.setBirthday(new Date());
            p.setMoney(MathHelper.multiplyDecimal(i, 2.3f, 2));
            p.setName("猫咪名" + i);
            p.setStreak(i % 2 == 0 ? "灰白相间" : "纯白");
            p.setSkin("皮肤狗");
            p.setWeight(i + 20);
            p.setHasCert(i % 3 == 0);
            p.setCreateDate(LocalDateTime.now());
            pets.add(p);
            // System.out.println(p);
        }
        File f = new File("D:\\datacenter\\cat20000.xlsx");

        try (ExcelCreator creator = new ExcelCreator(); FileOutputStream out = new FileOutputStream(f)) {
            creator.fillEntityData(pets);
            if (!f.exists()) {
                f.createNewFile();
            }
            creator.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long t = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
        System.out.println("耗时:" + t);
        System.in.read();
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
    public void testExportMap() {
        List<LinkedHashMap<String,Object>> pets = new ArrayList<>(300);
        for (int i = 0; i < 300; i++) {
            LinkedHashMap<String,Object> m = new LinkedHashMap<String,Object>();
            m.put("age", 18);
            m.put("name", "唯唯诺诺" + i);
            m.put("birthday", new Date());
            m.put("skin", "白皮肤");
            m.put("salary", new BigDecimal("322" + i));
            pets.add(m);
        }
        File f = new File("D:\\datacenter\\mapexport.xls");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            // 使用快捷静态方法
            ExcelCreator.export(pets, new String[] { "年龄","姓名","生日","皮肤","薪资" }, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 数组导出
     */
    public void testExportArray() {

        File f = new File("D:\\datacenter\\arrayexport.xls");
        List<Object[]> datas = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            Object[] obj = new Object[] { "张三三",i % 2 == 0 ? "男" : "女",new Date(),new BigDecimal(i * 200) };
            datas.add(obj);
        }
        try (ExcelCreator xlsCreator = new ExcelCreator(); FileOutputStream out = new FileOutputStream(f);) {
            if (!f.exists()) {
                f.createNewFile();
            }
            xlsCreator.fillArrayData(new String[] { "姓名","性别","时间","工资" }, datas);
            xlsCreator.write(out);
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
        ExcelImportor xlsImport = null;
        try {
            xlsImport = new ExcelImportor("D:\\datacenter\\cat20000.xlsx");
            xlsImport.setStepValid(false);
            xlsImport.setStartRowIndex(1);
            LinkedHashSet<Cat> results = xlsImport.importSheet(Cat.class);
            for (Cat pet : results) {
                System.out.println(pet);
            }
        } catch (ExcelAccessException e) {
            // 验证错误
            Map<Integer,String> xMap = xlsImport.getErrorMap();
            if (MapHelper.isNotEmpty(xMap)) {
                System.out.println(xMap.values()
                        .toString());
            } else {
                e.printStackTrace();
            }
        } finally {
            xlsImport.close();
        }
    }

}
