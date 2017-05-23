package com.ghgcn.xxx.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.jflame.toolkit.exception.BusinessException;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.MapHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.entity.TableInfo;
import com.baomidou.mybatisplus.enums.SqlMethod;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.SqlHelper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;
import com.ghgcn.xxx.service.IBaseService;

/**
 * service层实现基类
 * 
 * @author yucan.zhang
 * @param <M> mapper类型
 * @param <T> entity类型
 */
public class BaseServiceImpl<M extends BaseMapper<T>,T> implements IBaseService<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected M baseMapper;

    /**
     * <p>
     * 判断数据库操作是否成功
     * </p>
     * <p>
     * 注意！！ 该方法为 Integer 判断，不可传入 int 基本类型
     * </p>
     *
     * @param result 数据库操作返回影响条数
     * @return boolean
     */
    protected static boolean retBool(Integer result) {
        return SqlHelper.retBool(result);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> currentModleClass() {
        return ReflectionKit.getSuperClassGenricType(getClass(), 1);
    }

    /**
     * <p>
     * 批量操作 SqlSession
     * </p>
     */
    SqlSession sqlSessionBatch() {
        return SqlHelper.sqlSessionBatch(currentModleClass());
    }

    /**
     * 获取SqlStatement
     *
     * @param sqlMethod
     * @return
     */
    String sqlStatement(SqlMethod sqlMethod) {
        return SqlHelper.table(currentModleClass()).getSqlStatement(sqlMethod.getMethod());
    }

    /**
     * 插入对象,忽略null值
     * 
     * @param entity 实体对象
     * @return
     */
    @Override
    public boolean saveIgnoreNull(T entity) {
        return retBool(baseMapper.insert(entity));
    }

    /**
     * 插入对象
     * 
     * @param entity 实体对象
     * @return
     */
    @Override
    public boolean save(T entity) {
        return retBool(baseMapper.insertAllColumn(entity));
    }

    /**
     * 批量插入,batch size=30
     * 
     * @param entityList
     * @return
     * @throws BusinessException 批处理异常
     */
    @Override
    public void batchSave(List<T> entityList) {
        batchSave(entityList, 30);
    }

    /**
     * 批量插入
     *
     * @param entityList
     * @param batchSize
     * @return
     * @throws BusinessException 批处理异常
     */
    public void batchSave(List<T> entityList, int batchSize) {
        if (CollectionHelper.isNotEmpty(entityList)) {
            if (batchSize < 5) {
                batchSize = 30;
            }
            try (SqlSession batchSqlSession = sqlSessionBatch()) {
                int size = entityList.size();
                String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
                for (int i = 0; i < size; i++) {
                    batchSqlSession.insert(sqlStatement, entityList.get(i));
                    if (i >= 1 && i % batchSize == 0) {
                        batchSqlSession.flushStatements();
                    }
                }
                batchSqlSession.flushStatements();
            } catch (Exception e) {
                throw new BusinessException(e);
            }
        } else {
            logger.warn("batch insert list is empty");
        }
    }

    /**
     * 插入或更新,判断主键值存在则更新,否则插入
     *
     * @param entity 实体对象
     * @return boolean
     * @throws BusinessException 未找到主键
     */

    @Override
    public boolean saveOrUpdate(T entity) {
        if (null != entity) {
            Class<?> cls = entity.getClass();
            TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
            if (null != tableInfo && StringUtils.isNotEmpty(tableInfo.getKeyProperty())) {
                Object idVal = ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
                if (idVal == null || "".equals(idVal)) {
                    return save(entity);
                } else {
                    return updateById(entity);
                }
            } else {
                throw new BusinessException("未找到主键id");
            }
        }
        return false;
    }

    /**
     * 批量插入或更新,batch size=30,判断主键值存在则更新,否则插入
     * 
     * @param entityList
     * @return
     */
    @Override
    public void batchSaveOrUpdate(List<T> entityList) {
        batchSaveOrUpdate(entityList, 30);
    }

    /**
     * 批量插入或更新,判断主键值存在则更新,否则插入
     * 
     * @param entityList
     * @param batchSize
     * @return
     * @throws BusinessException 批处理异常
     */
    public void batchSaveOrUpdate(List<T> entityList, int batchSize) {
        if (CollectionHelper.isNotEmpty(entityList)) {
            if (batchSize < 5) {
                batchSize = 30;
            }
            try (SqlSession batchSqlSession = sqlSessionBatch()) {
                int size = entityList.size();
                for (int i = 0; i < size; i++) {
                    saveOrUpdate(entityList.get(i));
                    if (i >= 1 && i % batchSize == 0) {
                        batchSqlSession.flushStatements();
                    }
                }
                batchSqlSession.flushStatements();
            } catch (Exception e) {
                throw new BusinessException(e);
            }
        } else {
            logger.warn("batch insertOrUpdate list is empty");
        }
    }

    /**
     * 按主键删除
     * 
     * @param id 主键
     * @return
     */
    @Override
    public boolean deleteById(Serializable id) {
        return retBool(baseMapper.deleteById(id));
    }

    /**
     * 按条件删除
     * 
     * @param columnMap 表字段 map 对象
     * @return
     */
    @Override
    public boolean deleteByMap(Map<String,Object> columnMap) {
        if (MapHelper.isEmpty(columnMap)) {
            throw new BusinessException("删除条件columnMap不能为空.");
        }
        return retBool(baseMapper.deleteByMap(columnMap));
    }

    /**
     * 按条件删除
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    @Override
    public boolean delete(Wrapper<T> wrapper) {
        return retBool(baseMapper.delete(wrapper));
    }

    /**
     * 按主键id批量删除
     * 
     * @param idList id列表
     * @return
     */
    @Override
    public boolean deleteByIds(List<? extends Serializable> idList) {
        return retBool(baseMapper.deleteBatchIds(idList));
    }

    /**
     * 按主键更新
     * 
     * @param entity 待更新实体对象
     * @return
     */
    @Override
    public boolean updateById(T entity) {
        return retBool(baseMapper.updateAllColumnById(entity));
    }

    /**
     * 按主键更新,忽略空值
     * 
     * @param entity 待更新实体对象
     * @return
     */
    @Override
    public boolean updateByIdIgnoreNull(T entity) {
        return retBool(baseMapper.updateById(entity));
    }

    /**
     * 按条件更新
     * 
     * @param entity 待更新实体对象
     * @param wrapper 条件对象封装
     * @return
     */
    @Override
    public boolean update(T entity, Wrapper<T> wrapper) {
        return retBool(baseMapper.update(entity, wrapper));
    }

    /**
     * 按主键批量更新.
     * 
     * @param entityList 待更新实体对象列表
     * @param batchSize 每批次元素个数
     * @return
     */
    public void updateBatchById(List<T> entityList, int batchSize) {
        if (CollectionHelper.isNotEmpty(entityList)) {
            if (batchSize < 5) {
                batchSize = 30;
            }
            try (SqlSession batchSqlSession = sqlSessionBatch()) {
                int size = entityList.size();
                String sqlStatement = sqlStatement(SqlMethod.UPDATE_BY_ID);
                for (int i = 0; i < size; i++) {
                    batchSqlSession.update(sqlStatement, entityList.get(i));
                    if (i >= 1 && i % batchSize == 0) {
                        batchSqlSession.flushStatements();
                    }
                }
                batchSqlSession.flushStatements();
            } catch (Exception e) {
                throw new BusinessException(e);
            }
        } else {
            logger.warn("batch update list is empty");
        }
    }

    /**
     * 按id查询
     * 
     * @param id
     * @return
     */
    @Override
    public T getById(Serializable id) {
        return baseMapper.selectById(id);
    }

    /**
     * 按id列表查询
     * 
     * @param idList id列表
     * @return
     */
    @Override
    public List<T> getByIds(List<? extends Serializable> idList) {
        return baseMapper.selectBatchIds(idList);
    }

    /**
     * 按条件查询
     * 
     * @param columnMap 表字段 map 对象
     * @return
     */
    @Override
    public List<T> selectByMap(Map<String,Object> columnMap) {
        return baseMapper.selectByMap(columnMap);
    }

    /**
     * 按条件查询单一对象
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    @Override
    public T selectOne(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectList(wrapper));
    }

    @Override
    public Map<String,Object> selectMap(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectMaps(wrapper));
    }

    @Override
    public Object selectObj(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectObjs(wrapper));
    }

    @Override
    public int selectCount(Wrapper<T> wrapper) {
        return SqlHelper.retCount(baseMapper.selectCount(wrapper));
    }

    @Override
    public List<T> selectList(Wrapper<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<T> selectPage(Page<T> page) {
        return selectPage(page, Condition.EMPTY);
    }

    @Override
    public List<Map<String,Object>> selectMaps(Wrapper<T> wrapper) {
        return baseMapper.selectMaps(wrapper);
    }

    @Override
    public List<Object> selectObjs(Wrapper<T> wrapper) {
        return baseMapper.selectObjs(wrapper);
    }

    @Override
    @SuppressWarnings({ "rawtypes","unchecked" })
    public Page<Map<String,Object>> selectMapsPage(Page page, Wrapper<T> wrapper) {
        SqlHelper.fillWrapper(page, wrapper);
        page.setRecords(baseMapper.selectMapsPage(page, wrapper));
        return page;
    }

    @Override
    public Page<T> selectPage(Page<T> page, Wrapper<T> wrapper) {
        SqlHelper.fillWrapper(page, wrapper);
        page.setRecords(baseMapper.selectPage(page, wrapper));
        return page;
    }

    @Override
    public void updateByIds(List<T> entityList) {
        updateBatchById(entityList, 30);
    }

}
