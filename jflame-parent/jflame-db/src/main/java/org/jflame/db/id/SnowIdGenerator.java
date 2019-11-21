package org.jflame.db.id;

import java.io.Serializable;

import org.jflame.commons.key.SnowflakeGenerator;
import org.jflame.db.DbEnvironment;
import org.jflame.db.metadata.TableMetaData;

public class SnowIdGenerator implements IdGenerator {

    private static SnowflakeGenerator snowId = new SnowflakeGenerator();

    @Override
    public Serializable generate(DbEnvironment dbEnv, TableMetaData metaData) {
        long newId = snowId.nextId();
        //可能存为String
        return metaData.getKey().getPropertyType() == String.class ? String.valueOf(newId) : newId;
    }

}
