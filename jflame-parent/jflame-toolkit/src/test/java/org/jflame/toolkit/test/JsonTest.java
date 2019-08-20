package org.jflame.toolkit.test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import org.jflame.toolkit.test.entity.MemberInfo;
import org.jflame.toolkit.util.JsonHelper;

import org.junit.Test;

public class JsonTest {

    /*  @Test
    public void test() {
        MemberInfo member = new MemberInfo();
        member.setAge(22);
        member.setAppNo("zpgo");
        member.setBirthday(new Date());
        member.setNickName("nickna");
        member.setIdcard("542254445");
        // System.out.println(JsonHelper.toJson(member));
        System.out.println(JSON.toJSONString(member, new PascalNameFilter()));
        MemberInfo member2 = JSON.parseObject(
                "{\"Age\":22,\"AppNo\":\"zpgo\",\"Birthday\":1557726403146,\"Idcard\":\"542254445\",\"NickName\":\"nickna\"}",
                MemberInfo.class);
        System.out.println(member2);
    }
    */
    @Test
    public void testJackson() {
        MemberInfo member = new MemberInfo();
        member.setAge(22);
        member.setAppNo("zpgo");
        member.setBirthday(new Date());
        member.setNickName("nickna");
        member.setIdcard("542254445");
        System.out.println(JsonHelper.toJson(member));
    }

    @Test
    public void testbase64() throws UnsupportedEncodingException {
        String t = "UsZYkO1k5ikbp+9Y6bbMPMXAPjek0U3WAhJwsL/V7n/gdNlfmq5J5Xkeontp+4+3BI2yl8lAEfaB6jKU0eFIuHYAYRi4An5H7CaN3rtz0pWsCd3SSGZFo07hyQr86wsw9pG6LFKKFPypyf3Fkri2+0uauS9zKA952tDbFwRSPWo8p6sJxgqiWp3xgVDFodMVWAqHxkJBFGAju32HH0LIKZ1bku2IP42ia+9cMEhRJ2x+J0ptrAHMMdieOhqWsAoTbARZSVkTZIcDuXaywb6Ks8ZSRJM0lbzSuf9MjhXxHnh+UKEUj1WWLYWuXl54PXgv4oUr/5DNKGaR+ys/l8rQyw==";
        /* String bt = Base64.getUrlEncoder()
                .encodeToString(t.getBytes());*/
        System.out.println(new String(Base64.getDecoder()
                .decode(t), StandardCharsets.UTF_8));
    }
}
