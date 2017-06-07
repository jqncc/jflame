package org.jflame.db;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.PageBean;

public interface IBaseDao {

    /**
     * 根据id查询
     * 
     * @param entityClazz
     * @param id
     * @return
     */
    <T> T getById(Class<T> entityClazz, Object id);

    /**
     * 查询,返回单个对象,无此对象返回null
     * 
     * @param sql 参数化sql
     * @param resultClazz 返回对象类型
     * @param params 参数值数组
     * @return
     */
    <T> T get(String sql, Class<T> entityClazz, Object... params);

    /**
     * 根据查询条件查询单个对象,无返回null
     * 
     * @param entityClazz 返回对象类型
     * @param where 参数化查询条件
     * @param params 参数值
     * @return
     */
    <T> T getByCriteria(Class<T> entityClazz, String where, Object[] params);

    /**
     * 执行带条件的查询,返回实体类列表
     * 
     * @param sql 参数化sql
     * @param elementType 返回列表元素类型,类型可以是基本数据类型的封装类
     * @param params 参数值数组
     * @return
     */
    <T> List<T> query(String sql, Class<T> elementType, Object... params);

    /**
     * 查询所有数据
     * 
     * @param elementType 查询实体类型
     * @return
     */
    <T> List<T> queryAll(Class<T> elementType);

    /**
     * 指定排序条件查询所有数据
     * 
     * @param elementType 查询实体类型
     * @param orderBy 排序
     * @return
     */
    <T> List<T> queryAll(Class<T> elementType, MultiOrder orderBy);

    /**
     * 根据条件查询，返回实体类列表
     * 
     * @param elementType 实体类型
     * @param where 条件sql
     * @param params 参数值
     * @return
     */
    <T> List<T> queryByCriteria(Class<T> elementType, String where, Object... params);

    /**
     * 根据条件查询，返回实体类列表
     * 
     * @param elementType 实体类型
     * @param where 条件sql
     * @param orderBy 排序
     * @param params 参数值
     * @return
     */
    <T> List<T> query(Class<T> elementType, String where, MultiOrder orderBy, Object... params);

    /**
     * 执行带条件的查询(使用命名参数sql),返回实体类列表
     * 
     * @param namedParamSql 命名参数sql
     * @param elementType 返回列表元素类型
     * @param paramMap 参数值map
     * @return
     */
    <T> List<T> queryNamedParam(String namedParamSql, Class<T> elementType, Map<String,Object> paramMap);

    /**
     * 执行带条件的查询,返回以列名和列值组成的map列表
     * 
     * @param sql 参数化sql
     * @param params 参数值数组
     * @return 以列名和列值组成的map列表
     */
    List<Map<String,Object>> queryForMap(String sql, Object... params);

    /**
     * 执行带条件的查询(使用命名参数sql),返回以列名和列值组成的map列表
     * 
     * @param namedParamSql 命名参数sql
     * @param paramMap 参数map
     * @return
     */
    List<Map<String,Object>> queryNamedParamForMap(String namedParamSql, Map<String,Object> paramMap);

    /**
     * 执行单列查询,返回唯一行单列值.sql语句应限制结果集为单行
     * 
     * @param sql 参数化sql
     * @param singleObjectType 返回的列结果类型
     * @param params 参数值数组
     * @return 如果无此列或列值为sql null都会返回null
     */
    <T> T querySingle(String sql, Class<T> singleObjectType, Object... params);

    /**
     * 执行单列查询,返回唯一行单列值.sql语句应限制结果集为单行
     * 
     * @param namedParamSql 命名参数sql
     * @param singleObjectType 返回的列结果类型
     * @param paramMap 参数值Map
     * @return
     */
    <T> T querySingleByNamedParam(String namedParamSql, Class<T> singleObjectType, Map<String,Object> paramMap);

    /**
     * 分页查询
     * 
     * @param pager 分页参数
     * @param sql 参数化sql语句,不含分页的limit
     * @param dataTypeClass 数据元素类型.如果是map.class数据将是由列名和值组成的map
     * @param params 查询参数
     * @return
     */
    <T> void queryPage(PageBean pager, String sql, Class<T> dataTypeClass, Object... params);

    /**
     * 分页查询,数据类型是List<Map<String,Object>>
     * 
     * @param pager
     * @param sql
     * @param params
     */
    void queryMapPage(PageBean pager, String sql, Object... params);

    /**
     * 分页查询,暂只支持mysql
     * 
     * @param pager 分页参数
     * @param sql 命名参数sql,不含分页的limit
     * @param paramMap 参数值Map
     * @return
     */
    <T> void queryPageByNamedParam(PageBean pager, String namedParamSql, Class<T> dataTypeClass,
            Map<String,Object> paramMap);

    /**
     * 保存一个实体对象.
     * <p>
     * 新对象且主键由数据库生成请匆给主键赋值.如果主键为数据库生成,将会返回生成的主键值并赋值进原对象
     * 
     * @param entity
     */
    <T> void save(T entity);

    /**
     * 保存对象. 可排除不需要插入的属性.默认使用了@column(insertable=false)注解也不被插入
     * 
     * @param entity
     * @param excludePropertys 显式指定不需要插入的属性名
     */
    <T> void save(T entity, String[] excludePropertys);

    /**
     * 批量保存
     * 
     * @param entities
     */
    <T> void batchSave(List<T> entities);

    /**
     * 更新,以主键作为更新条件
     * 
     * @param entity
     */
    <T> int update(T entity);

    /**
     * 更新实体对象,只更新指定的属性
     * 
     * @param entity
     * @param inculdeProperties 要更新的属性，即使属性有isupdatable=false注解属性仍更新注解
     */
    <T> int update(T entity, String[] inculdeProperties);

    /**
     * 更新实体对象，排除指定的属性
     * 
     * @param entity 待更新对象
     * @param exculdeProperties 要排队属性
     * @return
     */
    <T> int updateExcludeProps(T entity, String[] exculdeProperties);

    /**
     * 执行更新语句
     * 
     * @param sql
     * @param params
     * @return 返回影响的行数
     */
    int execute(String sql, Object... params);

    /**
     * 批量更新,返回影响行数
     * 
     * @param entities
     * @return
     */
    <T> int[] batchUpdate(List<T> entities);

    /**
     * 批量操作
     * 
     * @param sql
     * @param params 参数值
     */
    int[] batchUpdate(String sql, List<Object[]> params);

    /**
     * 根据id删除
     * 
     * @param entityClazz
     * @param id
     */
    <T,ID extends Serializable> int deleteById(Class<T> entityClazz, ID id);

    /**
     * 根据id数组删除
     * 
     * @param entityClazz 待删除的对象类型
     * @param ids id数组
     * @return
     */
    <T,ID extends Serializable> int deleteByIds(Class<T> entityClazz, ID[] ids);

}