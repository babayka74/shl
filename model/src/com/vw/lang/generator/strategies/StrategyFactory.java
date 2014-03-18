package com.vw.lang.generator.strategies;

import com.vw.lang.generator.strategies.undefined.StrategyForUndefinedEntities;
import com.vw.lang.generator.strategies.undefined.StrategyInstantiateEntityWhenUndefined;

/**
 * Instantiates available strategies
 * @author Oleg
 *
 */
public class StrategyFactory {

	/**
	 * Returns instance of strategy for encountered undefined entity
	 * @return
	 */
	public static StrategyForUndefinedEntities getStratgeyForUndefinedEntities() {
		return new StrategyInstantiateEntityWhenUndefined();
	}
}
