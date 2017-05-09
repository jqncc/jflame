package com.ghgcn.xxx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.exception.BusinessException;

import com.baomidou.mybatisplus.mapper.Wrapper;

/**
 * service基接口,继承ICrudService,IBaseQueryService接口
 * @author yucan.zhang
 *
 * @param <T>
 */
public interface IBaseService<T> extends ICrudService<T>,IBaseQueryService<T> {

    /**
     * 插入对象,忽略null值
     * 
     * @param entity 实体对象
     * @return
     */
    boolean saveIgnoreNull(T entity);

    /**
     * 批量插入,batch size=30
     * 
     * @param entityList
     * @return
     * @throws BusinessException 批处理异常
     */
    void saveBatch(List<T> entityList);

    /**
     * 批量插入
     *
     * @param entityList
     * @param batchSize
     * @return
     * @throws BusinessException 批处理异常
     */
    void saveBatch(List<T> entityList, int batchSize);

    /**
     * 插入或更新,主键值存在则更新,否则插入
     *
     * @param entity 实体对象
     * @return boolean
     * @throws BusinessException 未找到主键
     */
    boolean saveOrUpdate(T entity);

    /**
     * 批量插入或更新,batch size=30
     * 
     * @param entityList
     * @return
     */
    void saveOrUpdateBatch(List<T> entityList);

    /**
     * 批量插入或更新
     * 
     * @param entityList
     * @param batchSize
     * @return
     * @throws BusinessException 批处理异常
     */
    void saveOrUpdateBatch(List<T> entityList, int batchSize);

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
    boolean deleteBatchIds(List<? extends Serializable> idList);

    /**
     * 按主键更新,忽略空值
     * 
     * @param entity 待更新实体对象
     * @return
     */
    boolean updateByIdIgnoreNull(T entity);

    /**
     * 按条件更新
     * 
     * @param entity 待更新实体对象
     * @param wrapper 条件对象封装
     * @return
     */
    boolean update(T entity, Wrapper<T> wrapper);

    /**
     * 按主键批量更新.batch size=30
     * 
     * @param entityList 待更新实体对象列表
     * @return
     */
    void updateBatchById(List<T> entityList);

    /**
     * 按主键批量更新.
     * 
     * @param entityList 待更新实体对象列表
     * @param batchSize 每批次元素个数
     * @return
     */
    void updateBatchById(List<T> entityList, int batchSize);

}