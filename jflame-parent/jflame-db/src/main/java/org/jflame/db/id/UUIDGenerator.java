package org.jflame.db.id;

import java.io.Serializable;

import org.jflame.db.DbEnvironment;
import org.jflame.db.metadata.TableMetaData;
import org.jflame.toolkit.key.IDHelper;

public class UUIDGenerator implements IdGenerator {

    @Override
    public Serializable generate(DbEnvironment dbEnv, TableMetaData metaData) {
        return IDHelper.uuid();
    }

}
