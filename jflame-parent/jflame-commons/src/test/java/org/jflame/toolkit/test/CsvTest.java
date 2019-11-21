package org.jflame.toolkit.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jflame.commons.csv.CsvAccessException;
import org.jflame.commons.csv.CsvWriter;
import org.jflame.commons.util.MathHelper;
import org.jflame.toolkit.test.entity.Cat;

import org.junit.Test;

public class CsvTest {

    @Test
    public void testExport() throws CsvAccessException, IOException {
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
        CsvWriter.writeCsv(Files.newOutputStream(Paths.get("d:\\datacenter\\cat2000.csv")), pets);
        long t = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
        System.out.println("耗时:" + t);
    }

}
