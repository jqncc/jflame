package org.jflame.db.id;

import java.io.Serializable;

import org.jflame.db.DbEnvironment;
import org.jflame.db.metadata.TableMetaData;


public class SequenceGenerator implements IdGenerator {

    @Override
    public Serializable generate(DbEnvironment dbEnv,TableMetaData metaData) {
        return null;
    }

}
