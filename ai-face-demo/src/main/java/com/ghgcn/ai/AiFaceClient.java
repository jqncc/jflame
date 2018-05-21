package com.ghgcn.ai;

public interface AiFaceClient {

    /**
     * 人脸检测
     */
    void detect();

    /**
     * 新增用户
     */
    void addUser();

    /**
     * 新增用户组
     * 
     * @param groupId
     * @return
     */
    String addGroup(String groupId);

    /**
     * 获取用户信息
     * 
     * @param userId
     * @param groupId
     */
    void getUser(String userId, String groupId);

    /**
     * 获取用户所有人脸照片
     * 
     * @param userId
     * @param groupId
     */
    void getUserFaces(String userId, String groupId);

    /**
     * 真实身份验证
     */
    void personVerify();

    /**
     * 人脸搜索
     */
    void findUser();

    /**
     * 人脸比对
     */
    void matchFace();

}
