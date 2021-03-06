package org.jflame.toolkit.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jflame.commons.excel.ExcelAccessException;
import org.jflame.commons.excel.ExcelCreator;
import org.jflame.commons.excel.ExcelImportor;
import org.jflame.commons.excel.validator.ExcelValidationException;
import org.jflame.commons.util.MathHelper;
import org.jflame.toolkit.test.entity.Cat;

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
        System.in.read();
        long start = System.nanoTime();
        final int max = 100000;
        List<Cat> pets = new ArrayList<>(max);
        Cat p = null;
        // 生成导出数据
        for (int i = 0; i < max; i++) {
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

        // 快捷静态方法导出

        // ExcelCreator.export(pets, "D:\\datacenter\\cat20001.xlsx");
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
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
            xlsImport.setStartRowIndex(1);
            List<Cat> results = xlsImport.importSheet(Cat.class);
            for (Cat pet : results) {
                System.out.println(pet);
            }
        } catch (ExcelValidationException e) {
            System.out.println(e.getMessage());
        } catch (ExcelAccessException e) {
            e.printStackTrace();
        } finally {
            xlsImport.close();
        }
    }

    /**
     * 实体类导出到文件流
     * 
     * @throws IOException
     */
    @org.junit.Test
    public void testBatchExport() throws IOException {
        System.in.read();
        long start = System.nanoTime();

        File f = new File("D:\\datacenter\\cat10w.xlsx");
        final int row = 3000,max = 100000;
        try (ExcelCreator creator = new ExcelCreator(100); FileOutputStream out = new FileOutputStream(f)) {
            creator.batchFillEntityData(null, max, 3000, (page) -> {
                List<Cat> pets = new ArrayList<>(row);
                Cat p = null;
                // 生成导出数据
                int i = (page - 1) * row;
                int offset = page * row;
                if (offset > max) {
                    offset = max;
                }
                Date now = new Date();
                for (; i < offset; i++) {
                    p = new Cat();
                    p.setAge(i);
                    p.setBirthday(now);
                    p.setMoney(MathHelper.multiplyDecimal(i, 2.3f, 2));
                    p.setName("猫咪名" + i);
                    p.setStreak(i % 2 == 0 ? "灰白相间" : "纯白");
                    p.setSkin("皮肤狗");
                    p.setWeight(i + 20);
                    p.setHasCert(i % 3 == 0);
                    p.setCreateDate(LocalDateTime.now());
                    pets.add(p);
                }
                return pets;
            });

            if (!f.exists()) {
                f.createNewFile();
            }
            creator.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long t = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
        System.out.println(String.format("耗时:%d秒", t));

        // 快捷静态方法导出

        // ExcelCreator.export(pets, "D:\\datacenter\\cat20001.xlsx");
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
