package org.jflame.db.id.enhanced;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jflame.db.id.IdGenerationException;
import org.jflame.db.id.IntegralDataTypeHolder;


public class PooledLoOptimizer extends AbstractOptimizer {
	final Log log = LogFactory.getLog(PooledLoOptimizer.class);

	private static class GenerationState {
		// last value read from db source
		private IntegralDataTypeHolder lastSourceValue;
		// the current generator value
		private IntegralDataTypeHolder value;
		// the value at which we'll hit the db again
		private IntegralDataTypeHolder upperLimitValue;
	}

	/**
	 * Constructs a PooledLoOptimizer.
	 *
	 * @param returnClass The Java type of the values to be generated
	 * @param incrementSize The increment size.
	 */
	public PooledLoOptimizer(Class<?> returnClass, int incrementSize) {
		super( returnClass, incrementSize );
		if ( incrementSize < 1 ) {
			throw new IdGenerationException( "increment size cannot be less than 1" );
		}
		log.debug("incrementSize = " + incrementSize + ", returnClass = " +  returnClass.getName() );
	}

	
	public  synchronized  Serializable generate(AccessCallback callback) {
			final GenerationState generationState = locateGenerationState( callback.getTenantIdentifier() );
			if ( generationState.lastSourceValue == null
					|| ! generationState.value.lt( generationState.upperLimitValue ) ) {
				generationState.lastSourceValue = callback.getNextValue();
				generationState.upperLimitValue = generationState.lastSourceValue.copy().add( incrementSize );
				generationState.value = generationState.lastSourceValue.copy();
				// handle cases where initial-value is less that one (hsqldb for instance).
				while ( generationState.value.lt( 1 ) ) {
					generationState.value.increment();
				}
			}
			return generationState.value.makeValueThenIncrement();
	}

	private GenerationState noTenantState;
	private Map<String,GenerationState> tenantSpecificState;

	private GenerationState locateGenerationState(String tenantIdentifier) {
		if ( tenantIdentifier == null ) {
			if ( noTenantState == null ) {
				noTenantState = new GenerationState();
			}
			return noTenantState;
		}else {
			GenerationState state;
			if ( tenantSpecificState == null ) {
				tenantSpecificState = new ConcurrentHashMap<String, GenerationState>();
				state = new GenerationState();
				tenantSpecificState.put( tenantIdentifier, state );
			}else {
				state = tenantSpecificState.get( tenantIdentifier );
				if ( state == null ) {
					state = new GenerationState();
					tenantSpecificState.put( tenantIdentifier, state );
				}
			}
			return state;
		}
	}

	private GenerationState noTenantGenerationState() {
		if ( noTenantState == null ) {
			throw new IllegalStateException( "Could not locate previous generation state for no-tenant" );
		}
		return noTenantState;
	}

	
	public IntegralDataTypeHolder getLastSourceValue() {
		return noTenantGenerationState().lastSourceValue;
	}

	
	public boolean applyIncrementSizeToSourceValues() {
		return true;
	}
}
