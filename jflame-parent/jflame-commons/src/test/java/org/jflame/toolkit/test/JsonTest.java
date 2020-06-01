package org.jflame.toolkit.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.TypeRef;
import org.jflame.commons.util.json.Fastjsons;
import org.jflame.commons.util.json.Jacksons;
import org.jflame.toolkit.test.entity.Pet;
import org.jflame.toolkit.test.entity.Pig;

public class JsonTest {

    List<Pet> pets = new ArrayList<>();
    Pet pet = null;
    CallResult<Pig> result = new CallResult<>();
    Pig pig = null;

    @Before
    public void initEntity() {
        pet = new Pet("black cat", 2, "black", new Date(), new BigDecimal("122.5"));
        pet.setCreateDate(LocalDateTime.now());
        Pet pet1 = new Pet("white cat", 1, "white", new Date(), new BigDecimal("200.39"));
        pets.add(pet);
        pets.add(pet1);

        pig = new Pig(1, "litte Pig", 30, "white");
        result.setData(pig);
    }

    @Test
    public void testJackson() {
        Jacksons json = new Jacksons();
        // 泛型对象转换
        String jsonStr = json.toJson(result);
        String viewjsonStr = json.toJsonView(pet, org.jflame.toolkit.test.entity.Pet.View.class);
        System.out.println("viewjsonStr:" + viewjsonStr);
        System.out.println("result toJson:" + jsonStr);
        CallResult<Pig> result1 = json.parseObject(jsonStr, new TypeRef<CallResult<Pig>>() {
        });
        System.out.println("parseCallResult:" + result1);
        // 简单对象转换
        String petjsonStr = json.toJson(pet);
        System.out.println("pet toJson:" + petjsonStr);
        Pet pet1 = json.parseObject(petjsonStr, Pet.class);
        System.out.println("pet1 parseJson:" + pet1);
        // 启用格式化输出
        json.prettyPrint();
        json.ignoreNull(true);
        // 启用时间格式化输出
        json.dateFormat();
        // 输出null值属性

        System.out.println("pet pretty toJson:" + json.toJson(pet));

        String lstjsonStr = json.toJson(pets);
        System.out.println("lstjsonStr:" + lstjsonStr);

        List<Pet> petlist = json.parseList(lstjsonStr, Pet.class);
        System.out.println("petlist:" + petlist);

    }

    @Test
    public void testFastjson() {
        Fastjsons json = new Fastjsons();
        // 泛型对象转换
        String jsonStr = json.toJson(result);
        System.out.println("result toJson:" + jsonStr);
        CallResult<Pig> result1 = json.parseObject(jsonStr, new TypeRef<CallResult<Pig>>() {
        });
        System.out.println("parseCallResult:" + result1);
        // 简单对象转换
        String petjsonStr = json.toJson(pet);
        System.out.println("pet toJson:" + petjsonStr);
        Pet pet1 = json.parseObject(petjsonStr, Pet.class);
        System.out.println("pet1 parseJson:" + pet1);

        // 启用格式化输出
        json.prettyPrint();
        // 启用时间格式化输出
        json.dateFormat();
        // 输出null值属性
        json.ignoreNull(false);
        System.out.println("pet pretty toJson:" + json.toJson(pet));

        String lstjsonStr = json.toJson(pets);
        System.out.println("lstjsonStr:" + lstjsonStr);

        List<Pet> petlist = json.parseList(lstjsonStr, Pet.class);
        System.out.println("petlist:" + petlist);

        String viewjsonStr = json.toJsonView(pet, "test");
        System.out.println("viewjsonStr:" + viewjsonStr);
    }
}
