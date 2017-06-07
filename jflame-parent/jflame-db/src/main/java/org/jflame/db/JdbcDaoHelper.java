package org.jflame.db;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.db.id.IdGenerator;
import org.jflame.db.id.IdType;
import org.jflame.db.id.factory.DefaultIdGeneratorFactory;
import org.jflame.db.metadata.DefaultMetaDataProvider;
import org.jflame.db.metadata.IMetaDataProvider;
import org.jflame.db.metadata.TableMetaData;
import org.jflame.toolkit.common.bean.PageBean;
import org.jflame.toolkit.reflect.ReflectionHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 通用jdbc操作类. 对spring jdbc的封装
 * <p>
 * 默认实体属性名与数据表列名的转换规则是驼峰命名转下划线分隔形式. <br />
 * <p>
 * 支持参数化sql和命名参数sql查询,向外部暴露NamedParameterJdbcTemplate,JdbcOperations
 * <p>
 * 支持的数据库类型oracle,postgresql,mysql,sqlserver
 * 
 * @author zyc
 */
public class JdbcDaoHelper implements DbEnvironment, IBaseDao {

    private final Logger logger = LoggerFactory.getLogger(JdbcDaoHelper.class);

    private NamedParameterJdbcTemplate jdbcTemplate;
    private IMetaDataProvider metaDataProvider = new DefaultMetaDataProvider();

    private int batchSize = 30;// 批处理数量
    private Dialect curDbType = Dialect.MySql;
    private PlatformTransactionManager transactionManager;
    private DefaultIdGeneratorFactory idGeneratorFactory=new DefaultIdGeneratorFactory();

    /**
     * 根据id查询
     * 
     * @param entityClazz
     * @param id
     * @return
     */
    @Override
    public <T> T getById(final Class<T> entityClazz, Object id) {
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entityClazz);
        String sqlString = SimpleSqlBuilder.selectSql(metaData, metaData.getKey().getColumnName(), null);
        return get(sqlString, entityClazz, id);
    }

    /**
     * 查询,返回单个对象,无此对象返回null
     * 
     * @param sql 参数化sql
     * @param resultClazz 返回对象类型
     * @param params 参数值数组
     * @return
     */
    @Override
    public <T> T get(final String sql, final Class<T> entityClazz, final Object... params) {
        List<T> results = query(sql, entityClazz, params);
        return CollectionHelper.isEmpty(results) ? null : results.get(0);
    }

    /**
     * 根据查询条件查询单个对象,无返回null
     * 
     * @param entityClazz 返回对象类型
     * @param where 参数化查询条件
     * @param params 参数值
     * @return
     */
    @Override
    public <T> T getByCriteria(final Class<T> entityClazz, final String where, Object[] params) {
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entityClazz);
        String sqlString = SimpleSqlBuilder.selectSql(metaData, where, null);
        return get(sqlString, entityClazz, params);
    }

    /**
     * 执行带条件的查询,返回实体类列表
     * 
     * @param sql 参数化sql
     * @param elementType 返回列表元素类型,类型可以是基本数据类型的封装类
     * @param params 参数值数组
     * @return
     */
    @Override
    public <T> List<T> query(final String sql, final Class<T> elementType, final Object... params) {
        if (BeanUtils.isSimpleValueType(elementType))
            return jdbcTemplate.getJdbcOperations().queryForList(sql, elementType, params);
        else
            return jdbcTemplate.getJdbcOperations().query(sql, AnnonBeanPropertyRowMapper.newInstance(elementType),
                    params);
    }

    /**
     * 查询所有数据
     * 
     * @param elementType 查询实体类型
     * @return
     */
    @Override
    public <T> List<T> queryAll(final Class<T> elementType) {
        String sql = SimpleSqlBuilder.selectSql(metaDataProvider.extractTableMetaData(elementType), null, null);
        return jdbcTemplate.getJdbcOperations().query(sql, AnnonBeanPropertyRowMapper.newInstance(elementType));
    }

    /**
     * 指定排序条件查询所有数据
     * 
     * @param elementType 查询实体类型
     * @param orderBy 排序
     * @return
     */
    @Override
    public <T> List<T> queryAll(final Class<T> elementType, MultiOrder orderBy) {
        String sql = SimpleSqlBuilder.selectSql(metaDataProvider.extractTableMetaData(elementType), null, orderBy);
        return jdbcTemplate.getJdbcOperations().query(sql, AnnonBeanPropertyRowMapper.newInstance(elementType));
    }

    /**
     * 根据条件查询，返回实体类列表
     * 
     * @param elementType 实体类型
     * @param where 条件sql
     * @param params 参数值
     * @return
     */
    @Override
    public <T> List<T> queryByCriteria(final Class<T> elementType, String where, Object... params) {
        return query(elementType, where, null, params);
    }

    /**
     * 根据条件查询，返回实体类列表
     * 
     * @param elementType 实体类型
     * @param where 条件sql
     * @param orderBy 排序
     * @param params 参数值
     * @return
     */
    @Override
    public <T> List<T> query(final Class<T> elementType, String where, MultiOrder orderBy, Object... params) {
        String sql = SimpleSqlBuilder.selectSql(metaDataProvider.extractTableMetaData(elementType), where, orderBy);
        return jdbcTemplate.getJdbcOperations().query(sql, AnnonBeanPropertyRowMapper.newInstance(elementType), params);
    }

    /**
     * 执行带条件的查询(使用命名参数sql),返回实体类列表
     * 
     * @param namedParamSql 命名参数sql
     * @param elementType 返回列表元素类型
     * @param paramMap 参数值map
     * @return
     */
    @Override
    public <T> List<T> queryNamedParam(final String namedParamSql, final Class<T> elementType,
            final Map<String,Object> paramMap) {
        return jdbcTemplate.query(namedParamSql, new MapSqlParameterSource(paramMap),
                AnnonBeanPropertyRowMapper.newInstance(elementType));
    }

    /**
     * 执行带条件的查询,返回以列名和列值组成的map列表
     * 
     * @param sql 参数化sql
     * @param params 参数值数组
     * @return 以列名和列值组成的map列表
     */
    @Override
    public List<Map<String,Object>> queryForMap(final String sql, final Object... params) {
        return jdbcTemplate.getJdbcOperations().query(sql, new ColumnMapRowMapper(), params);
    }

    /**
     * 执行带条件的查询(使用命名参数sql),返回以列名和列值组成的map列表
     * 
     * @param namedParamSql 命名参数sql
     * @param paramMap 参数map
     * @return
     */
    @Override
    public List<Map<String,Object>> queryNamedParamForMap(final String namedParamSql,
            final Map<String,Object> paramMap) {
        return jdbcTemplate.query(namedParamSql, new MapSqlParameterSource(paramMap), new ColumnMapRowMapper());
    }

    /**
     * 执行单列查询,返回唯一行单列值.sql语句应限制结果集为单行
     * 
     * @param sql 参数化sql
     * @param singleObjectType 返回的列结果类型
     * @param params 参数值数组
     * @return 如果无此列或列值为sql null都会返回null
     */
    @Override
    public <T> T querySingle(String sql, final Class<T> singleObjectType, final Object... params) {
        try {
            return jdbcTemplate.getJdbcOperations().queryForObject(sql, singleObjectType, params);
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    /**
     * 执行单列查询,返回唯一行单列值.sql语句应限制结果集为单行
     * 
     * @param namedParamSql 命名参数sql
     * @param singleObjectType 返回的列结果类型
     * @param paramMap 参数值Map
     * @return
     */
    @Override
    public <T> T querySingleByNamedParam(String namedParamSql, final Class<T> singleObjectType,
            final Map<String,Object> paramMap) {
        List<T> lst = jdbcTemplate.queryForList(namedParamSql, new MapSqlParameterSource(paramMap), singleObjectType);
        return CollectionHelper.isEmpty(lst) ? null : lst.get(0);
    }

    /**
     * 分页查询
     * 
     * @param pager 分页参数
     * @param sql 参数化sql语句,不含分页的limit
     * @param dataTypeClass 数据元素类型.如果是map.class数据将是由列名和值组成的map
     * @param params 查询参数
     * @return
     */
    @Override
    public <T> void queryPage(final PageBean pager, String sql, final Class<T> dataTypeClass, final Object... params) {
        if (pager == null || sql == null)
            throw new IllegalArgumentException();
        if (pager.isAutoCount()) {
            Long count = querySingle(prepareCountSql(sql), Long.class, params);
            pager.setMaxRowCount(count);
        }
        if ((pager.isAutoCount() && pager.getMaxRowCount() > 0) || !pager.isAutoCount()) {
            sql = SimpleSqlBuilder.pageSql(sql, pager.getStartIndex(), pager.getPageSize(), curDbType);
            logger.debug(sql);
            if (Map.class.equals(dataTypeClass)) {
                List<Map<String,Object>> lst = queryForMap(sql, params);
                pager.setPageData(lst);
            } else {
                List<T> lst = query(sql, dataTypeClass, params);
                pager.setPageData(lst);
            }
        }
    }

    /**
     * 分页查询,数据类型是List<Map<String,Object>>
     * 
     * @param pager
     * @param sql
     * @param params
     */
    @Override
    public void queryMapPage(final PageBean pager, String sql, final Object... params) {
        queryPage(pager, sql, Map.class, params);
    }

    /**
     * 分页查询,暂只支持mysql
     * 
     * @param pager 分页参数
     * @param sql 命名参数sql,不含分页的limit
     * @param paramMap 参数值Map
     * @return
     */
    @Override
    public <T> void queryPageByNamedParam(final PageBean pager, String namedParamSql, final Class<T> dataTypeClass,
            final Map<String,Object> paramMap) {
        if (pager == null || namedParamSql == null)
            throw new IllegalArgumentException();
        if (pager.isAutoCount()) {
            Long count = querySingleByNamedParam(prepareCountSql(namedParamSql), Long.class, paramMap);
            pager.setMaxRowCount(count);
        }
        if ((pager.isAutoCount() && pager.getMaxRowCount() > 0) || !pager.isAutoCount()) {
            // namedParamSql = namedParamSql + " limit " + pager.getStartIndex() + ","
            // + pager.getPageSize();
            namedParamSql = SimpleSqlBuilder.pageSql(namedParamSql, pager.getStartIndex(), pager.getPageSize(),
                    curDbType);
            logger.debug(namedParamSql);
            if (Map.class.equals(dataTypeClass)) {
                List<Map<String,Object>> lst = queryNamedParamForMap(namedParamSql, paramMap);
                pager.setPageData(lst);
            } else {
                List<T> lst = queryNamedParam(namedParamSql, dataTypeClass, paramMap);
                pager.setPageData(lst);
            }
        }
    }

    /**
     * 保存一个实体对象.
     * <p>
     * 新对象且主键由数据库生成请匆给主键赋值.如果主键为数据库生成,将会返回生成的主键值并赋值进原对象
     * 
     * @param entity
     */
    @Override
    public <T> void save(T entity) {
        save(entity, null);
    }

    /**
     * 保存对象. 可排除不需要插入的属性.默认使用了@column(insertable=false)注解也不被插入
     * 
     * @param entity
     * @param excludePropertys 显式指定不需要插入的属性名
     */
    @Override
    public <T> void save(T entity, String[] excludePropertys) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        TableMetaData metaData = metaDataProvider.extractTableMetaData(beanWrapper);
        Serializable newId=idGeneratorFactory.generate(this, metaData);
        String sqlNamedParam = SimpleSqlBuilder.insertSql(metaData, excludePropertys);// 生成sql插入语句
        logger.debug(sqlNamedParam);
        if (IdGenerator.KEY_HOLDER.equals(newId)) {
            // 获取自增主键赋值
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int c = jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity), keyHolder);
            if (c > 0) {
                beanWrapper.setPropertyValue(metaData.getKey().getPropertyName(),
                        keyHolder.getKeys().values().iterator().next());
            }
        } else {
            beanWrapper.setPropertyValue(metaData.getKey().getPropertyName(),newId);
            jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity));
        }
    }

    /**
     * 批量保存
     * 
     * @param entities
     */
    @Override
    public <T> void batchSave(List<T> entities) {
        int size = entities.size();
        if (size == 0) {
            return;
        } else if (size == 1) {
            save(entities.get(0));
            return;
        }
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entities.get(0).getClass());
        String sqlNamedParam = SimpleSqlBuilder.insertSql(metaData, null);// 生成sql插入语句
        logger.debug(sqlNamedParam);
        if (metaData.getKey().getIdType() != IdType.IDENTITY) {
            BeanPropertySqlParameterSource[] sqlSources = new BeanPropertySqlParameterSource[size];
            Serializable[] newIds=new Serializable[size];
            int i=0;
            //先生成批量id,再赋值
            for (i = 0; i < size; i++) {
                newIds[i]=idGeneratorFactory.generate(this, metaData);
            }
            T entity=null;
            BeanWrapper beanWrapper;
            for (i = 0; i < size; i++) {
                entity=entities.get(i);
                beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
                beanWrapper.setPropertyValue(metaData.getKey().getPropertyName(),newIds[i]);
                sqlSources[i] = new BeanPropertySqlParameterSource(entities.get(i));
            }
            jdbcTemplate.batchUpdate(sqlNamedParam, sqlSources);
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            BeanWrapper bw;
            for (T entity : entities) {
                int c = jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity), keyHolder);
                if (c > 0) {
                    bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);
                    bw.setPropertyValue(metaData.getKey().getPropertyName(),
                            keyHolder.getKeys().values().iterator().next());
                }
            }
        }
    }

    /**
     * 更新,以主键作为更新条件
     * 
     * @param entity
     */
    @Override
    public <T> int update(T entity) {
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entity.getClass());
        String sqlNamedParam = SimpleSqlBuilder.updateSql(metaData, metaData.getKey().getPropertyName());
        logger.debug(sqlNamedParam);
        return jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity));
    }

    /**
     * 更新实体对象,只更新指定的属性
     * 
     * @param entity
     * @param inculdeProperties 要更新的属性，即使属性有isupdatable=false注解属性仍更新注解
     */
    @Override
    public <T> int update(T entity, String[] inculdeProperties) {
        if (inculdeProperties == null || inculdeProperties.length == 0)
            throw new IllegalArgumentException("参数inculdeProperties不能为空");
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entity.getClass());
        String sqlNamedParam = SimpleSqlBuilder.updateSql(metaData, metaData.getKey().getPropertyName(),
                inculdeProperties);
        logger.debug(sqlNamedParam);
        return jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity));
    }

    /**
     * 更新实体对象，排除指定的属性
     * 
     * @param entity 待更新对象
     * @param exculdeProperties 要排队属性
     * @return
     */
    @Override
    public <T> int updateExcludeProps(T entity, String[] exculdeProperties) {
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entity.getClass());
        String sqlNamedParam = SimpleSqlBuilder.updateWithExcludePropSql(metaData, metaData.getKey().getPropertyName(),
                exculdeProperties);
        logger.debug(sqlNamedParam);
        return jdbcTemplate.update(sqlNamedParam, new BeanPropertySqlParameterSource(entity));
    }

    /**
     * 执行更新语句
     * 
     * @param sql
     * @param params
     * @return 返回影响的行数
     */
    @Override
    public int execute(final String sql, final Object... params) {
        logger.debug(sql);
        return jdbcTemplate.getJdbcOperations().update(sql, params);
    }

    /**
     * 批量更新,返回影响行数
     * 
     * @param entities
     * @return
     */
    @Override
    public <T> int[] batchUpdate(List<T> entities) {
        if (CollectionHelper.isEmpty(entities))
            return new int[0];
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entities.get(0).getClass());
        String sqlNamedParam = SimpleSqlBuilder.updateSql(metaData, metaData.getKey().getPropertyName());
        int size = entities.size();
        logger.debug(sqlNamedParam + ",count:" + size);
        BeanPropertySqlParameterSource[] sqlSources = new BeanPropertySqlParameterSource[size];
        for (int i = 0; i < size; i++) {
            sqlSources[i] = new BeanPropertySqlParameterSource(entities.get(i));
        }
        return jdbcTemplate.batchUpdate(sqlNamedParam, sqlSources);
    }

    /**
     * 批量操作
     * 
     * @param sql
     * @param params 参数值
     */
    @Override
    public int[] batchUpdate(final String sql, final List<Object[]> params) {
        // 注:jdbcTemplate.batchUpdate该方法是一次提交所有,非分批提交
        if (!CollectionHelper.isEmpty(params) && params.size() > batchSize) {
            int[] affected = new int[0];
            List<Object[]> tmpList = new ArrayList<>(batchSize);
            final int size = params.size();
            int[] tmpArr;
            for (int i = 0; i < size; i++) {
                tmpList.add(params.get(i));
                if ((i > 0 && i % batchSize == 0) || i == size - 1) {
                    tmpArr = jdbcTemplate.getJdbcOperations().batchUpdate(sql, tmpList);
                    affected = ArrayUtils.addAll(affected, tmpArr);
                    tmpList.clear();
                }
            }
            return affected;
        } else {
            return jdbcTemplate.getJdbcOperations().batchUpdate(sql, params);
        }
    }

    /**
     * 根据id删除
     * 
     * @param entityClazz
     * @param id
     */
    @Override
    public <T,ID extends Serializable> int deleteById(final Class<T> entityClazz, ID id) {
        TableMetaData metaData = metaDataProvider.extractTableMetaData(entityClazz);
        String sqlNamedParam = SimpleSqlBuilder.deleteSql(metaData, metaData.getKey().getColumnName());
        return jdbcTemplate.getJdbcOperations().update(sqlNamedParam, id);
    }

    /**
     * 根据id数组删除
     * 
     * @param entityClazz 待删除的对象类型
     * @param ids id数组
     * @return
     */
    @Override
    public <T,ID extends Serializable> int deleteByIds(final Class<T> entityClazz, ID[] ids) {
        if (ids.length == 1) {
            return deleteById(entityClazz, ids[0]);
        } else if (ids.length > 1) {
            String sqlTpl = "delete from {0} where {1} in ({2})";
            String where;
            if (ids[0] instanceof Number) {
                where = StringHelper.join(ids);
            } else {
                where = '\'' + StringHelper.join(ids, "','") + '\'';
            }
            TableMetaData metaData = metaDataProvider.extractTableMetaData(entityClazz);
            sqlTpl = MessageFormat.format(sqlTpl, metaData.getTableName(), metaData.getKey().getColumnName(), where);
            return jdbcTemplate.getJdbcOperations().update(sqlTpl);
        }
        return 0;
    }

    private String prepareCountSql(String originalSql) {
        int d = StringUtils.indexOfIgnoreCase(originalSql, "select distinct");
        String countSql;
        if (d > -1) {
            countSql = "SELECT COUNT(" + originalSql.substring(d + 7, originalSql.indexOf(",", d)) + ") from "
                    + StringHelper.substringAfterIgnoreCase(originalSql, "from");
        } else {
            countSql = "SELECT COUNT(*) FROM " + StringHelper.substringAfterIgnoreCase(originalSql, "from");
        }
        // select子句与order by子句会影响count查询,进行简单的排除.
        countSql = StringHelper.substringBeforeIgnoreCase(countSql, "ORDER BY");
        logger.debug(countSql);
        return countSql;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void setMetaDataProvider(IMetaDataProvider metaDataProvider) {
        this.metaDataProvider = metaDataProvider;
    }

    @Override
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionTemplate getTransactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public void setBatchSize(int batch_size) {
        this.batchSize = batch_size;
    }

    /**
     * 返回NamedParameterJdbcTemplate
     * 
     * @return
     */
    public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * 返回JdbcOperations,相当于JdbcTemplate
     * 
     * @return
     */
    public JdbcOperations getJdbcOperation() {
        return jdbcTemplate.getJdbcOperations();
    }

    @Override
    public Dialect getDialect() {
        return curDbType;
    }

    @Override
    public void setDialect(Dialect dbDialect) {
        curDbType = dbDialect;
    }

}
