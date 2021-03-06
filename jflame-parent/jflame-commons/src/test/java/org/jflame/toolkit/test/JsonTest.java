package org.jflame.toolkit.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.jflame.commons.json.Fastjsons;
import org.jflame.commons.json.Jacksons;
import org.jflame.commons.json.Jsons;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.TypeRef;
import org.jflame.toolkit.test.entity.MemberInfo;
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
        pet.setWeight(9007199254740998d);
        pet.setCreateDate(LocalDateTime.now());
        Pet pet1 = new Pet("white cat", 1, "white", new Date(), new BigDecimal("200.39"));
        pets.add(pet);
        pets.add(pet1);

        pig = new Pig(1, "litte Pig", 30, "white");
        result.setData(pig);
    }

    @Test
    public void testTransient() {
        MemberInfo mem = new MemberInfo();
        mem.setUserName("user1");
        mem.setPasswd("mypasswd");// Transient field
        System.out.println("before json:" + mem);
        Jacksons json = new Jacksons();
        String memJson = json.toJson(mem);
        System.out.println(memJson);

        MemberInfo mem1 = json.parseObject(memJson, MemberInfo.class);
        System.out.println(mem1);

        String jsonStr = "{\"userName\":\"user2\",\"passwd\":\"newpasswd\"}";
        MemberInfo mem2 = json.parseObject(jsonStr, MemberInfo.class);
        System.out.println(mem2);

    }

    @Test
    public void testJackson() {
        Jacksons json = new Jacksons(true);
        // 泛型对象转换
        String jsonStr = json.toJson(result);
        String viewjsonStr = json.toJsonView(pet, org.jflame.toolkit.test.entity.Pet.View.class);
        System.out.println("viewjsonStr:" + viewjsonStr);
        System.out.println("result toJson:" + jsonStr);
        CallResult<Pig> result1 = json.parseObject(jsonStr, new TypeRef<CallResult<Pig>>() {
        });
        System.out.println("parseCallResult:" + result1);

        String petjsonStrFilter = json.toJsonFilter(pet, true, new String[] { "age","skin" });
        System.out.println("pet toJsonFilter:" + petjsonStrFilter);

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
        Fastjsons json = new Fastjsons(true);
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

    @Test
    public void testTypeRef() {
        CallResult<List<Pet>> results = new CallResult<>();
        results.setData(pets);
        Fastjsons fjson = new Fastjsons();
        Jacksons jason = new Jacksons();

        String jst = fjson.toJson(results);
        System.out.println(jst);

        System.out.println("fastjson parse:");
        CallResult<List<Pet>> resultf = fjson.parseObject(jst, new TypeRef<CallResult<List<Pet>>>() {
        });
        System.out.println(fjson.toJson(resultf));

        System.out.println("Jacksons parse:");

        CallResult<List<Pet>> resultj = jason.parseObject(jst, new TypeRef<CallResult<List<Pet>>>() {
        });

        System.out.println(fjson.toJson(resultj));

        System.out.println("fastjson parse:");
        CallResult<List<Pet>> resultf1 = parseCallResult(fjson, jst, Pet.class);
        System.out.println(fjson.toJson(resultf1));

        System.out.println("Jacksons parse:");

        CallResult<List<Pet>> resultj1 = parseCallResult(jason, jst, Pet.class);
        System.out.println(fjson.toJson(resultj1));

    }

    <T> CallResult<List<T>> parseCallResult(Jsons jsons, String jst, Class<T> listEleClazz) {
        return jsons.parseObject(jst, new TypeRef<CallResult<List<T>>>(listEleClazz) {
        });
    }
}
