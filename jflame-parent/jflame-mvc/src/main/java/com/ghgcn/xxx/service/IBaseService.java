package com.ghgcn.xxx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;

/**
 * service基接口,继承ICrudService接口
 * 
 * @author yucan.zhang
 * @param <T>
 */
public interface IBaseService<T,PK extends Serializable> extends ICrudService<T,PK> {

    /**
     * 批量插入,<b>该方法不适合 Oracle</b>
     * 
     * @param entityList
     * @return
     * @throws DataAccessException 批处理异常
     */
    void saveBatch(List<T> entityList);

    /**
     * 批量插入或更新
     * 
     * @param entityList
     * @return
     * @throws DataAccessException 批处理异常
     */
    void saveOrUpdateBatch(List<T> entityList);

    /**
     * 批量插入或更新.<b>选择性修改(根据非空和FieldStrategy策略更新)</b>
     * 
     * @param entityList
     * @throws DataAccessException 批处理异常
     */
    void saveOrUpdateOptionalBatch(List<T> entityList);

    /**
     * 按主键批量更新.
     * 
     * @param entityList 待更新实体对象列表
     * @return
     * @throws DataAccessException 批处理异常
     */
    void updateByIds(List<T> entityList);

    /**
     * 按主键批量更新.<b>选择性修改(根据非空和FieldStrategy策略更新)</b>
     * 
     * @param entityList 待更新实体对象列表
     * @return
     * @throws DataAccessException 批处理异常
     */
    void updateOptionalByIds(List<T> entityList);

    /**
     * 按主键更新单列值
     * 
     * @param column 列名
     * @param value 值
     * @param id 主键
     * @return
     */
    boolean updateColumnById(String column, Object value, PK id);

    /**
     * 按条件更新指定列名
     * 
     * @param entity 待更新实体对象
     * @param wrapper 更新条件和要更新的列,属性updateColumns指定更新列
     * @return
     */
    boolean updateColumns(T entity, Wrapper<T> wrapper);

    /**
     * 按条件删除
     * 
     * @param columnMap 表字段 map 对象
     * @return
     */
    boolean deleteByMap(Map<String,Object> columnMap);

    /**
     * 按条件删除
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    boolean delete(Wrapper<T> wrapper);

    /**
     * 按主键id批量删除
     * 
     * @param idList id列表
     * @return
     */
    boolean deleteByIds(List<PK> idList);

    /**
     * 按id列表查询
     * 
     * @param idList id列表
     * @return
     */
    List<T> findByIds(List<PK> idList);

    /**
     * 按条件查询单一对象
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    T get(Wrapper<T> wrapper);

    /**
     * 按条件查询
     * 
     * @param columnMap 表字段 map对象
     * @return
     */
    List<T> findByMap(Map<String,Object> columnMap);

    /**
     * 根据条件查询单一对象,对象以Map返回
     * 
     * @param wrapper 条件对象封装
     * @return Map
     */
    Map<String,Object> findForMap(Wrapper<T> wrapper);

    /**
     * 根据条件查询,对象以Map集合返回
     * 
     * @param wrapper
     * @return List&lt;Map&lt;String,Object&gt;&gt;
     */
    List<Map<String,Object>> findForMaps(Wrapper<T> wrapper);

    Page<Map<String,Object>> findForMapsPage(Page<Map<String,Object>> page, Wrapper<T> wrapper);

    /**
     * 根据条件查询单列
     * 
     * @param wrapper
     * @return Object
     */
    Object findForObject(Wrapper<T> wrapper);

    /**
     * 根据条件查询单列值集合
     * 
     * @param wrapper
     * @return
     */
    List<Object> findForObjects(Wrapper<T> wrapper);

    /**
     * 根据条件查询记录数
     * 
     * @param wrapper
     * @return
     */
    long selectCount(Wrapper<T> wrapper);

    /**
     * 分页查询
     * 
     * @param page
     * @return
     */
    Page<T> findPage(Page<T> page);

    /**
     * 按条件分页查询
     * 
     * @param page
     * @return
     */
    Page<T> findPage(Page<T> page, Wrapper<T> wrapper);
}