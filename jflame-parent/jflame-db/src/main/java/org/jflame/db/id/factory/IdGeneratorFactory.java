package org.jflame.db.id.factory;
import java.util.Map;

import org.jflame.db.Dialect;
import org.springframework.jdbc.core.JdbcTemplate;



public interface IdGeneratorFactory {
	/**
	 * Get the dialect.
	 *
	 * @return the dialect
	 */
	public Dialect getDialect();



	/**
	 * Creates a new IdentifierGenerator object.
	 *
	 * @param entityFactory the entity factory
	 * @param strategy the strategy
	 * @param generator the generator
	 * @param config the config
	 * @return the identifier generator
	 */
	public IdGeneratorFactory createIdentifierGenerator(JdbcTemplate jdbcTemplate, String strategy,String generator,  Map<String,Object> config);

	/**
	 * Retrieve the class that will be used as the {@link IdentifierGenerator} for the given strategy.
	 *
	 * @param strategy The strategy
	 * @param generator the generator
	 * @return The generator class.
	 */
	public Class getIdentifierGeneratorClass(String strategy, String generator);
}
