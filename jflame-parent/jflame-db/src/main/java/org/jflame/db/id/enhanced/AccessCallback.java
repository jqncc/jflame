package org.jflame.db.id.enhanced;

import org.jflame.db.id.IntegralDataTypeHolder;

public interface AccessCallback {
	/**
	 * Retrieve the next value from the underlying source.
	 *
	 * @return The next value.
	 */
	public IntegralDataTypeHolder getNextValue();

	/**
	 * Obtain the tenant identifier (multi-tenancy), if one, associated with this callback.
	 *
	 * @return The tenant identifier
	 */
	public String getTenantIdentifier();
}
