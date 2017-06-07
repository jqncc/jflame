package org.jflame.db.id.enhanced;

import org.jflame.db.id.IdGenerationException;

public abstract class AbstractOptimizer implements Optimizer {

    protected final Class<?> returnClass;
    protected final int incrementSize;

    /**
     * Construct an optimizer
     *
     * @param returnClass The expected id class.
     * @param incrementSize The increment size
     */
    AbstractOptimizer(Class<?> returnClass, int incrementSize) {
        if (returnClass == null) {
            throw new IdGenerationException("return class is required");
        }
        this.returnClass = returnClass;
        this.incrementSize = incrementSize;
    }

    /**
     * Getter for property 'returnClass'. This is the Java class which is used to represent the id (e.g. {@link Long}).
     *
     * @return Value for property 'returnClass'.
     */
    public final Class<?> getReturnClass() {
        return returnClass;
    }

    public final int getIncrementSize() {
        return incrementSize;
    }
}
