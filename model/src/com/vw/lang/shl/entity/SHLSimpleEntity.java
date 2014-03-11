package com.vw.lang.shl.entity;

import com.vw.lang.shl.context.SHLContext;

/**
 * SHL's simple entity (projection of VWMLSimpleEntity)
 * @author Oleg
 *
 */
public class SHLSimpleEntity extends SHLEntity {

	public SHLSimpleEntity(Object hashId) {
		super(hashId);
	}
	
	public SHLSimpleEntity(SHLContext context, Object hashId, Object id, String readableId) {
		super(context, hashId, id, readableId);
	}
	
	@Override
	public String buildReadableId() {
		if (getReadableId() == null) {
			setReadableId((String)getId());
		}
		return getReadableId();
	}
}
