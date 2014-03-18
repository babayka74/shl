package com.vw.lang.generator.strategies.undefined;

import org.apache.log4j.Logger;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.shl.entity.SHLEntityBuilder;
import com.vw.lang.shl.repositories.SHLContextsRepository;

/**
 * Undefined entity is instantiated
 * @author Oleg
 *
 */
public class StrategyInstantiateEntityWhenUndefined extends StrategyForUndefinedEntities {

	// internal logger
	private static Logger logger = Logger.getLogger(StrategyInstantiateEntityWhenUndefined.class);
	
	@Override
	public SHLEntity undefinedEntityEncountered(Object entityId, SHLContext context) {
		SHLEntity entity = SHLEntityBuilder.buildSimpleEntity(entityId, context.getContextName(), context);
		context.associateEntity(entity);
		if (logger.isDebugEnabled()) {
			logger.debug("The entity '" + entityId + "' detected us undefined and defined on context '" + context.getContextName() + "'");
		}
		return entity;
	}

	@Override
	public SHLContext undefinedContextEncountered(Object contextId) {
		if (logger.isDebugEnabled()) {
			logger.debug("The context '" + contextId + "' detected us undefined and created");
		}
		return SHLContextsRepository.instance().createContextIfNotExists(contextId);
	}
}
