package com.vw.lang.shl.entity;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.sink.java.link.VWMLLinkIncrementalIterator;
import com.vw.lang.sink.utils.ComplexEntityNameBuilder;

/**
 * SHL's complex entity (projection of VWMLComplexEntity)
 * @author Oleg
 *
 */
public class SHLComplexEntity extends SHLEntity {

	public SHLComplexEntity(Object hashId) {
		super(hashId);
	}
	
	public SHLComplexEntity(SHLContext context, Object hashId, Object id, String readableId) {
		super(context, hashId, id, readableId);
	}
	
	@Override
	public boolean isMarkedAsComplexEntity() {
		return true;
	}
	
	@Override
	public String buildReadableId() {
		if (getReadableId() == null) {
			ComplexEntityNameBuilder ce = ComplexEntityNameBuilder.instance();
			ce.setNameBuilderVisitor(getNameBuilderVisitor());
			assembleReadableId(ce, this);
			setReadableId(ce.build());
			ce.clear();
		}
		return getReadableId();
	}
	
	protected void assembleReadableId(ComplexEntityNameBuilder ce, SHLEntity entity) {
		ce.startProgress();
		VWMLLinkIncrementalIterator it = entity.getLink().acquireLinkedObjectsIterator();
		if (it != null) {
			for(SHLEntity le = (SHLEntity)entity.getLink().peek(it); le != null; le = (SHLEntity)entity.getLink().peek(it)) {
				if (le.isMarkedAsComplexEntity()) {
					assembleReadableId(ce, le);
				}
				else {
					ce.addObjectId(le.buildReadableId());
				}
			}
		}
		ce.stopProgress();
	}
}
