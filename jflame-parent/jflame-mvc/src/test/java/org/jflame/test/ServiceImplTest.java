package org.jflame.test;

import javax.annotation.Resource;

import org.jflame.toolkit.util.JsonHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ghgcn.xxx.common.enums.SexEnum;
import com.ghgcn.xxx.common.enums.UserStatusEnum;
import com.ghgcn.xxx.entity.SysFunction;
import com.ghgcn.xxx.entity.UserInfo;
import com.ghgcn.xxx.service.ISysFunctionService;
import com.ghgcn.xxx.service.IUserInfoService;
import com.ghgcn.xxx.service.impl.SysFunctionServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/spring.xml" })
public class ServiceImplTest {
    
    @Resource(name="userInfoServiceImpl")
    private IUserInfoService userServiceImpl;
    @Resource(name="sysFunctionServiceImpl")
    private ISysFunctionService sysFunctionService;
    @Test
    public void testSave() {
        UserInfo user=new UserInfo();
        //user.setCreateDate(new Date());
        user.setPassword("1dfdf1321312312");
        //user.setSex(1);
        user.setSex(SexEnum.FEMALE);
        user.setUserStatus(UserStatusEnum.LOCKED);
        user.setUserName("好名字中国造");
        //userServiceImpl.save(user);
        System.out.println(JsonHelper.toJson(user));
    }
    
    
    @Test
    public void testGet() {
        UserInfo user=userServiceImpl.getById(3);
        System.out.println(user.getSex());
    }

    
    @Test
    public void testSaveFun() {
        SysFunction fun=new SysFunction();
        fun.setFunCode("user_create_add");
        fun.setFunDesc("tst desc");
        fun.setFunName("fun name");
        fun.setOrderNum(1);
        fun.setParentIds(new Integer[]{1,2,3});
        fun.setFunUrl("/userdo");
        fun.setFunType(1);
        sysFunctionService.save(fun);
    }
}
