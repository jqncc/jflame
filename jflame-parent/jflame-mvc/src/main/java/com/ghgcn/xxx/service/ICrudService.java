package com.ghgcn.xxx.service;

import java.io.Serializable;

/**
 * 
 * 只包含增、删、改、查操作的service接口
 * @author yucan.zhang
 *
 * @param <T>
 */
public interface ICrudService<T> {
    /**
     * 插入对象
     * 
     * @param entity 实体对象
     * @return
     */
    boolean save(T entity);
    
    /**
     * 按主键更新
     * @param entity 待更新实体对象
     * @return
     */
    boolean updateById(T entity);
    
    /**
     * 按主键删除
     * @param id 主键
     * @return
     */
    boolean deleteById(Serializable id);
    
    /**
     * 按id查询
     * @param id
     * @return
     */
    T selectById(Serializable id);
}
