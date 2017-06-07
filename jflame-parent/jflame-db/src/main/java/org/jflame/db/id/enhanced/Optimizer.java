package org.jflame.db.id.enhanced;

import java.io.Serializable;

import org.jflame.db.id.IntegralDataTypeHolder;


public interface Optimizer {
	/**
	 * Generate an identifier value accounting for this specific optimization.
	 *
	 * All known implementors are synchronized. Consider carefully if a new
	 * implementation could drop this requirement.
	 *
	 * @param callback Callback to access the underlying value source.
	 * @return The generated identifier value.
	 */
	public Serializable generate(AccessCallback callback);

	/**
	 * A common means to access the last value obtained from the underlying
	 * source.  This is intended for testing purposes, since accessing the
	 * underlying database source directly is much more difficult.
	 *
	 * @return The last value we obtained from the underlying source;
	 * null indicates we have not yet consulted with the source.
	 */
	public IntegralDataTypeHolder getLastSourceValue();

	/**
	 * Retrieves the defined increment size.
	 *
	 * @return The increment size.
	 */
	public int getIncrementSize();

	/**
	 * Are increments to be applied to the values stored in the underlying
	 * value source?
	 *
	 * @return True if the values in the source are to be incremented
	 * according to the defined increment size; false otherwise, in which
	 * case the increment is totally an in memory construct.
	 */
	public boolean applyIncrementSizeToSourceValues();
}
