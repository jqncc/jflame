package org.jflame.db.id.factory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.jflame.db.DbEnvironment;
import org.jflame.db.Dialect;
import org.jflame.db.id.IdGenerationException;
import org.jflame.db.id.IdGenerator;
import org.jflame.db.id.IdType;
import org.jflame.db.id.IdentityGenerator;
import org.jflame.db.id.SequenceGenerator;
import org.jflame.db.id.SnowIdGenerator;
import org.jflame.db.id.TableSequenceGenerator;
import org.jflame.db.id.UUIDGenerator;
import org.jflame.db.metadata.TableMetaData;

public class IdGeneratorFactory {

    private ConcurrentHashMap<IdType,IdGenerator> generatorMap = new ConcurrentHashMap<>();

    public IdGeneratorFactory() {
        register(IdType.TABLE, new TableSequenceGenerator());
        register(IdType.IDENTITY, new IdentityGenerator());
        register(IdType.SNOWFLAKE_ID, new SnowIdGenerator());
        register(IdType.UUID, new UUIDGenerator());
        register(IdType.SEQUENCE, new SequenceGenerator());
    }

    void register(IdType strategy, IdGenerator generator) {
        generatorMap.put(strategy, generator);
    }

    public Serializable generate(DbEnvironment dbEnv, TableMetaData metaData) {
        IdType type = metaData.getKey().getIdType();
        if (dbEnv.getDialect() != Dialect.Oracle || dbEnv.getDialect() != Dialect.PostgreSQL) {
            if (metaData.getKey().getIdType() == IdType.SEQUENCE) {
                throw new IdGenerationException("不支持的id生成策略" + type);
            }
        }
        IdGenerator generator = generatorMap.get(type);
        if (generator != null) {
            return generator.generate(dbEnv, metaData);
        } else {
            throw new IdGenerationException("不支持的id生成策略" + type);
        }
    }
}
