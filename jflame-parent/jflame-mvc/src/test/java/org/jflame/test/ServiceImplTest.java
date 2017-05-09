package org.jflame.test;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ghgcn.xxx.common.enums.SexEnum;
import com.ghgcn.xxx.common.enums.UserStatusEnum;
import com.ghgcn.xxx.entity.UserInfo;
import com.ghgcn.xxx.service.IUserInfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/spring.xml" })
public class ServiceImplTest {
    
    @Resource(name="userInfoServiceImpl")
    private IUserInfoService userServiceImpl;
    @Test
    public void testSave() {
        UserInfo user=new UserInfo();
        //user.setCreateDate(new Date());
        user.setPassword("1dfdf1321312312");
        //user.setSex(1);
        user.setSex(SexEnum.FEMALE);
        user.setUserStatus(UserStatusEnum.LOCKED);
        user.setUserName("好名字中国造");
        userServiceImpl.save(user);
    }
    
    
    @Test
    public void testGet() {
        UserInfo user=userServiceImpl.selectById(3);
        System.out.println(user.getSex());
    }

}
