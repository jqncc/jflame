package com.ghgcn.xxx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;

/**
 * 
 * 只包含查询方法的service基接口
 * @author yucan.zhang
 *
 * @param <T>
 */
public interface IBaseQueryService<T> {

    /**
     * 按id查询
     * 
     * @param id
     * @return
     */
    T getById(Serializable id);

    /**
     * 按id列表查询
     * 
     * @param idList id列表
     * @return
     */
    List<T> getByIds(List<? extends Serializable> idList);

    /**
     * 按条件查询
     * 
     * @param columnMap 表字段 map 对象
     * @return
     */
    List<T> selectByMap(Map<String,Object> columnMap);

    /**
     * 按条件查询单一对象
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    T selectOne(Wrapper<T> wrapper);

    Map<String,Object> selectMap(Wrapper<T> wrapper);

    Object selectObj(Wrapper<T> wrapper);

    int selectCount(Wrapper<T> wrapper);

    List<T> selectList(Wrapper<T> wrapper);

    Page<T> selectPage(Page<T> page);

    List<Map<String,Object>> selectMaps(Wrapper<T> wrapper);

    List<Object> selectObjs(Wrapper<T> wrapper);

    Page<Map<String,Object>> selectMapsPage(Page page, Wrapper<T> wrapper);

    Page<T> selectPage(Page<T> page, Wrapper<T> wrapper);
}
