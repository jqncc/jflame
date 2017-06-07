package org.jflame.db;

import javax.sql.DataSource;

import org.jflame.db.metadata.IMetaDataProvider;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public interface DbEnvironment {

    void setDataSource(DataSource dataSource);

    void setBatchSize(int batchSize);

    int getBatchSize();

    void setMetaDataProvider(IMetaDataProvider metaDataProvider);

    void setTransactionManager(PlatformTransactionManager transactionManager);

    public TransactionTemplate getTransactionTemplate();

    public Dialect getDialect();

    public void setDialect(Dialect dbDialect);

    public JdbcOperations getJdbcOperation();

}