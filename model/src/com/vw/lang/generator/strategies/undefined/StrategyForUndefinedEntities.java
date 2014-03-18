package com.vw.lang.generator.strategies.undefined;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;

/**
 * What should have to do in case if undefined entity or context found
 * @author Oleg
 *
 */
public abstract class StrategyForUndefinedEntities {
	/**
	 * Undefined entity encountered
	 * @param entityId
	 * @param context
	 */
	public abstract SHLEntity undefinedEntityEncountered(Object entityId, SHLContext context);

	/**
	 * Undefined context encountered
	 * @param entity
	 * @param contextId
	 */
	public abstract SHLContext undefinedContextEncountered(Object contextId);
}
