package com.ghgcn.xxx.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;
import org.jflame.toolkit.exception.DataAccessException;
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
 * @param <PK> 主键类型
 */
public class BaseServiceImpl<M extends BaseMapper<T,PK>,T,PK extends Serializable> implements IBaseService<T,PK> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected M baseMapper;

    private final int BATCH_SIZE = 30;

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

    @Override
    public boolean save(T entity) {
        return retBool(baseMapper.insertAllColumn(entity));
    }

    @Override
    public boolean saveOptional(T entity) {
        return retBool(baseMapper.insert(entity));
    }

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
                throw new DataAccessException("未找到主键id");
            }
        }
        return false;
    }

    @Override
    public boolean saveOrUpdateOptional(T entity) {
        if (null != entity) {
            Class<?> cls = entity.getClass();
            TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
            if (null != tableInfo && StringUtils.isNotEmpty(tableInfo.getKeyProperty())) {
                Object idVal = ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
                if (idVal == null || "".equals(idVal)) {
                    return saveOptional(entity);
                } else {
                    return updateOptionalById(entity);
                }
            } else {
                throw new DataAccessException("未找到主键id");
            }
        }
        return false;
    }

    @Override
    public void saveBatch(List<T> entityList) {
        if (CollectionHelper.isEmpty(entityList)) {
            logger.error("错误,试图插入一个空的集合entityList");
            return;
        }
        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            int size = entityList.size();
            String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
            for (int i = 0; i < size; i++) {
                batchSqlSession.insert(sqlStatement, entityList.get(i));
                if (i >= 1 && i % BATCH_SIZE == 0) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.flushStatements();
        } catch (Exception e) {
            throw new DataAccessException("批量插入错误", e);
        }
    }

    @Override
    public void saveOrUpdateOptionalBatch(List<T> entityList) {
        saveOrUpdateBatch(entityList, true);
    }

    @Override
    public void saveOrUpdateBatch(List<T> entityList) {
        saveOrUpdateBatch(entityList, false);
    }

    void saveOrUpdateBatch(List<T> entityList, boolean isOptional) {
        if (CollectionHelper.isEmpty(entityList)) {
            logger.error("错误,试图插入或更新一个空的集合entityList");
            return;
        }
        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            int size = entityList.size();
            for (int i = 0; i < size; i++) {
                if (isOptional) {
                    saveOptional(entityList.get(i));
                } else {
                    save(entityList.get(i));
                }
                if (i >= 1 && i % BATCH_SIZE == 0) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.flushStatements();
        } catch (Exception e) {
            throw new DataAccessException("批量操作错误", e);
        }
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

    @Override
    public boolean updateOptionalById(T entity) {
        return retBool(baseMapper.updateById(entity));
    }

    @Override
    public boolean update(T entity, Wrapper<T> wrapper) {
        return retBool(baseMapper.update(entity, wrapper));
    }

    @Override
    public void updateByIds(List<T> entityList) {
        updateBatchById(entityList, false);
    }

    @Override
    public void updateOptionalByIds(List<T> entityList) {
        updateBatchById(entityList, true);
    }

    private void updateBatchById(List<T> entityList, boolean isOptional) {
        if (CollectionHelper.isEmpty(entityList)) {
            logger.error("错误,试图更新一个空的集合entityList");
            return;
        }
        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            int size = entityList.size();
            SqlMethod sqlMethod = isOptional ? SqlMethod.UPDATE_BY_ID : SqlMethod.UPDATE_ALL_COLUMN_BY_ID;
            String sqlStatement = sqlStatement(sqlMethod);
            for (int i = 0; i < size; i++) {
                MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
                param.put("et", entityList.get(i));
                batchSqlSession.update(sqlStatement, param);
                if (i >= 1 && i % BATCH_SIZE == 0) {
                    batchSqlSession.flushStatements();
                }
            }
            batchSqlSession.flushStatements();
        } catch (Exception e) {
            throw new DataAccessException("批量更新错误", e);
        }
    }

    @Override
    public boolean updateColumnById(String column, Object value, PK id) {
        return retBool(baseMapper.updateColumnById(column, value, id));
    }

    @Override
    public boolean updateColumns(T entity, Wrapper<T> wrapper) {
        if (ArrayUtils.isEmpty(wrapper.getUpdateColumns())) {
            throw new IllegalArgumentException("请指定要更新的列");
        }
        return retBool(baseMapper.updateColumns(entity, wrapper));
    }

    /**
     * 按主键删除
     * 
     * @param id 主键
     * @return
     */
    @Override
    public boolean deleteById(PK id) {
        return retBool(baseMapper.deleteById(id));
    }

    @Override
    public boolean deleteByIds(List<PK> idList) {
        return retBool(baseMapper.deleteBatchIds(idList));
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
            throw new DataAccessException("删除条件columnMap不能为空.");
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
     * 按id查询
     * 
     * @param id
     * @return
     */
    @Override
    public T getById(PK id) {
        return baseMapper.selectById(id);
    }

    /**
     * 按条件查询单一对象
     * 
     * @param wrapper 条件对象封装
     * @return
     */
    @Override
    public T get(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectList(wrapper));
    }

    /**
     * 按id列表查询
     * 
     * @param idList id列表
     * @return
     */
    @Override
    public List<T> findByIds(List<PK> idList) {
        return baseMapper.selectBatchIds(idList);
    }

    @Override
    public List<T> findForList(Wrapper<T> wrapper) {
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<T> findAll() {
        return findForList(null);
    }

    @Override
    public long selectCount(Wrapper<T> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public Object findForObject(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectObjs(wrapper));
    }

    @Override
    public List<Object> findForObjects(Wrapper<T> wrapper) {
        return baseMapper.selectObjs(wrapper);
    }

    @Override
    public List<T> findByMap(Map<String,Object> columnMap) {
        return baseMapper.selectByMap(columnMap);
    }

    @Override
    public Map<String,Object> findForMap(Wrapper<T> wrapper) {
        return SqlHelper.getObject(baseMapper.selectMaps(wrapper));
    }

    @Override
    public List<Map<String,Object>> findForMaps(Wrapper<T> wrapper) {
        return baseMapper.selectMaps(wrapper);
    }

    @Override
    public Page<T> findPage(Page<T> page, Wrapper<T> wrapper) {
        SqlHelper.fillWrapper(page, wrapper);
        page.setRecords(baseMapper.selectPage(page, wrapper));
        return page;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<T> findPage(Page<T> page) {
        return findPage(page, Condition.EMPTY);
    }

    @Override
    public Page<Map<String,Object>> findForMapsPage(Page<Map<String,Object>> page, Wrapper<T> wrapper) {
        SqlHelper.fillWrapper(page, wrapper);
        page.setRecords(baseMapper.selectMapsPage(page, wrapper));
        return page;
    }
}
