package org.jflame.db.id;

import java.io.Serializable;

import org.jflame.db.DbEnvironment;
import org.jflame.db.metadata.TableMetaData;

public interface IdGenerator {

    String KEY_HOLDER = "key_holder";

    public Serializable generate(DbEnvironment dbEnv,TableMetaData metaData);
}
