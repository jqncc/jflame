package org.jflame.toolkit.test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jflame.toolkit.test.Payserver.Adult;
import org.jflame.toolkit.test.Payserver.Child;
import org.jflame.toolkit.test.entity.Person;
import org.jflame.toolkit.test.entity.UserInfo;
import org.jflame.toolkit.valid.ValidatorHelper;
import org.junit.Test;

public class ValidTest {

    @Test
    public void test() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName("test@");
        userInfo.setPassword("231");
        userInfo.setConfirmPwd("dfdsf");
        Map<String,String> errMsg = ValidatorHelper.validBean(userInfo);
        for (Entry<String,String> kv : errMsg.entrySet()) {
            System.out.println(kv.getKey() + "=" + kv.getValue());
        }
    }

    @Test
    public void testPayload() {
        Person p = new Person();
        p.setAge(1);
        p.setHeight(120);
        p.setName("charles");
        p.setSex("男");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Person>> errors = validator.validate(p);
        boolean severeError = false;
        for (ConstraintViolation<Person> violation : errors) {
            Set<Class<? extends Payload>> payloads = violation.getConstraintDescriptor()
                    .getPayload();
            for (Class<? extends Payload> payload : payloads) {
                if (payload == Child.class) {
                    severeError = true;
                    System.out.println("Error: " + violation.getPropertyPath() + " " + violation.getMessage());
                } else if (payload == Adult.class) {
                    System.out.println("Info: " + violation.getPropertyPath() + " " + violation.getMessage());
                }
            }
        }
    }

    @Test
    public void testValid() {
        String[] tels = new String[] { "123551","13124557854","12124557854","16542557844","1842557844","1642557844",
                "18x2557844" };
        for (String string : tels) {
            System.out.println(string + ":" + ValidatorHelper.isMobileOrTel(string, 0));
        }
    }

    @Test
    public void testPerson() {
        Person p = new Person();
        p.setAge(13);
        p.setHeight(120);
        p.setName("charles");
        p.setSex("男");
        p.setWeight(new BigDecimal("300.23"));
        Map<String,String> result = ValidatorHelper.validBean(p);
        System.out.println(result);
    }
}
