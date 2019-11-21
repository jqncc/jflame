package org.jflame.db.id;

import java.io.Serializable;

import org.jflame.commons.key.IDHelper;
import org.jflame.db.DbEnvironment;
import org.jflame.db.metadata.TableMetaData;

public class UUIDGenerator implements IdGenerator {

    @Override
    public Serializable generate(DbEnvironment dbEnv, TableMetaData metaData) {
        return IDHelper.uuid();
    }

}
