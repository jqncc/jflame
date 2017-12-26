package com.ghgcn.xxx.service;

import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.mapper.Wrapper;

/**
 * 只包含常用crud操作的service接口
 * 
 * @author yucan.zhang
 * @param <T> 实体类泛型
 * @param <PK> 主键泛型
 */
public interface ICrudService<T,PK extends Serializable> {

    /**
     * 插入对象
     * 
     * @param entity 实体对象
     * @return boolean true=成功
     */
    boolean save(T entity);

    /**
     * 选择性插入对象(根据非空和FieldStrategy策略插入)
     * 
     * @param entity 实体对象
     * @return boolean true=成功
     */
    boolean saveOptional(T entity);

    /**
     * 按主键更新
     * 
     * @param entity 待更新实体对象
     * @return boolean true=成功
     */
    boolean updateById(T entity);

    /**
     * <p>
     * 按主键更新, 选择性修改(根据非空和FieldStrategy策略更新)
     * </p>
     *
     * @param entity 实体对象
     * @return boolean true=成功
     */
    boolean updateOptionalById(T entity);

    /**
     * 按条件更新
     * 
     * @param entity 待更新实体对象
     * @param wrapper 条件对象封装
     * @return boolean true=成功
     */
    boolean update(T entity, Wrapper<T> wrapper);

    /**
     * 插入或更新,主键值存在则更新,否则插入
     *
     * @param entity 实体对象
     * @return boolean true=成功
     */
    boolean saveOrUpdate(T entity);

    /**
     * 插入或更新,主键值存在则更新,否则插入.选择性修改(根据非空和FieldStrategy策略更新)
     *
     * @param entity 实体对象
     * @return boolean true=成功
     */
    boolean saveOrUpdateOptional(T entity);

    /**
     * 按主键删除
     * 
     * @param id 主键
     * @return boolean true=成功
     */
    boolean deleteById(PK id);

    /**
     * 按id查询
     * 
     * @param id 主键
     * @return
     */
    T getById(PK id);

    /**
     * 查询所有
     * 
     * @return
     */
    List<T> findAll();

    /**
     * 按条件查询列表
     *
     * @param wrapper 实体包装类 {@link Wrapper}
     * @return
     */
    List<T> findForList(Wrapper<T> wrapper);

}
