package com.vw.lang.shl.entity;

import com.vw.lang.shl.context.SHLContext;

/**
 * Simple entity's builder
 * @author Oleg
 *
 */
public class SHLEntityBuilder {
	/**
	 * Builds simple SHL entity
	 * @param id
	 * @param hashId
	 * @param context
	 * @return
	 */
	public static SHLEntity buildSimpleEntity(Object id, Object hashId, SHLContext context) {
		return new SHLSimpleEntity(context, hashId, id, null);
	}

	/**
	 * Builds complex SHL entity
	 * @param id
	 * @param hashId
	 * @param context
	 * @return
	 */
	public static SHLEntity buildComplexEntity(Object id, Object hashId, SHLContext context) {
		return new SHLComplexEntity(context, hashId, id, null);
	}
}
