package org.jflame.db.id;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jflame.db.DbEnvironment;
import org.jflame.db.annotations.TableGenerator;
import org.jflame.db.id.enhanced.AccessCallback;
import org.jflame.db.id.enhanced.Optimizer;
import org.jflame.db.id.enhanced.PooledLoOptimizer;
import org.jflame.db.metadata.TableMetaData;
import org.jflame.toolkit.util.StringHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class TableSequenceGenerator implements IdGenerator {

    private final static Map<String,Optimizer> seqValueHolderCache = new ConcurrentHashMap<>();

    final String SQL_UPDATE = "update {tableName} set {valueColumnName} where {valueColumnName}=? and {pkColumnName}=?";

    protected String buildSelectQuery(SeqInfo seq) {
        final String SQL_QUERY = "select " + seq.valueColumnName + " from " + seq.tableName + " where "
                + seq.pkColumnName + "=? for update";
        return SQL_QUERY;
    }

    protected String buildInsertQuery(SeqInfo seq) {
        final String SQL_INSERT = "insert into " + seq.tableName + " (" + seq.pkColumnName + "," + seq.valueColumnName
                + ") values (?,?)";
        return SQL_INSERT;
    }

    protected String buildUpdateQuery(SeqInfo seq) {
        final String SQL_UPDATE = "update " + seq.tableName + " set " + seq.valueColumnName + " where "
                + seq.valueColumnName + "=? and " + seq.seqName + "=?";
        return SQL_UPDATE;
    }

    class SeqInfo {

        String seqName;
        int increment;
        int initialValue;
        String tableName;
        String pkColumnName;
        String valueColumnName;

        public SeqInfo(TableMetaData metaData) {
            Annotation[] idAnnots = metaData.getKey().getPropertyAnnotations();
            TableGenerator tableAnnot = null;
            for (Annotation annot : idAnnots) {
                if (annot.annotationType() == TableGenerator.class) {
                    tableAnnot = (TableGenerator) annot;
                }
            }
            this.seqName = tableAnnot.seqName();
            if (StringHelper.isEmpty(seqName)) {
                seqName = metaData.getTableName() + "_seq";
            }
            this.increment = tableAnnot.increment();
            this.initialValue = tableAnnot.initialValue();
            this.tableName = tableAnnot.tableName();
            this.pkColumnName = tableAnnot.pkColumnName();
            this.valueColumnName = tableAnnot.valueColumnName();

        }
    }

    final Optimizer getOptimizer(SeqInfo seqInfo, final TableMetaData metaData) {
        Optimizer optimizer = seqValueHolderCache.get(metaData.getTableName());
        if (optimizer == null) {
            optimizer = new PooledLoOptimizer(metaData.getKey().getPropertyType(), seqInfo.increment);
            seqValueHolderCache.put(metaData.getTableName(), optimizer);
        }
        return optimizer;
    }

    @Override
    public Serializable generate(final DbEnvironment dbEnv, final TableMetaData metaData) {
        final SeqInfo seqInfo = new SeqInfo(metaData);
        final Optimizer optimizer = getOptimizer(seqInfo, metaData);
        final TransactionTemplate txTemplate = dbEnv.getTransactionTemplate();
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return optimizer.generate(new AccessCallback() {

            public IntegralDataTypeHolder getNextValue() {
                IntegralDataTypeHolder idHolder = null;

                // 开始事务，如果出现状况则回滚
                idHolder = txTemplate.execute(new TransactionCallback<IntegralDataTypeHolder>() {

                    public IntegralDataTypeHolder doInTransaction(TransactionStatus ts) {
                        // 如果成功，事务被提交
                        return dbEnv.getJdbcOperation().execute(new ConnectionCallback<IntegralDataTypeHolder>() {

                            public IntegralDataTypeHolder doInConnection(Connection connection)
                                    throws SQLException, DataAccessException {
                                final IntegralDataTypeHolder value = IdGeneratorHelper
                                        .getIntegralDataTypeHolder(metaData.getKey().getPropertyType());
                                int rows;
                                do {
                                    final PreparedStatement selectPS = prepareStatement(connection,
                                            buildSelectQuery(seqInfo));
                                    try {
                                        selectPS.setString(1, seqInfo.seqName);
                                        final ResultSet selectRS = selectPS.executeQuery();
                                        if (!selectRS.next()) {
                                            // 如果找不到则插入一条新的记录
                                            value.initialize(seqInfo.initialValue);
                                            final PreparedStatement insertPS = prepareStatement(connection,
                                                    buildInsertQuery(seqInfo));
                                            try {
                                                insertPS.setString(1, seqInfo.seqName);
                                                value.bind(insertPS, 2);
                                                rows = insertPS.executeUpdate();
                                            } finally {
                                                insertPS.close();
                                            }
                                        } else {
                                            value.initialize(selectRS, 1);
                                        }
                                        selectRS.close();
                                    } catch (SQLException e) {
                                        throw e;
                                    } finally {
                                        selectPS.close();
                                    }

                                    final PreparedStatement updatePS = prepareStatement(connection,
                                            buildUpdateQuery(seqInfo));
                                    try {
                                        final IntegralDataTypeHolder updateValue = value.copy();
                                        if (optimizer.applyIncrementSizeToSourceValues()) {
                                            updateValue.add(seqInfo.increment);
                                        } else {
                                            updateValue.increment();
                                        }
                                        updateValue.bind(updatePS, 1);
                                        value.bind(updatePS, 2);
                                        updatePS.setString(3, seqInfo.seqName);
                                        rows = updatePS.executeUpdate();

                                    } catch (SQLException e) {
                                        throw e;
                                    } finally {
                                        updatePS.close();
                                    }
                                } while (rows == 0);
                                return value;
                            }
                        });
                    }
                });
                return idHolder;
            }

            public String getTenantIdentifier() {
                return seqInfo.seqName;
            }
        });
    }

    private PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

}
