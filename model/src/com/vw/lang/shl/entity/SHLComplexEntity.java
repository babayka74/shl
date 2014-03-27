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
	
	@Override
	public String asVWMLCode(String prefix, boolean start) {
		return prepareVWMLCode(prefix, start);
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
	
	protected String prepareVWMLCode(String prefix, boolean firstTime) {
		String str = prefix;
		if (firstTime) {
			if (getNameBuilderVisitor() != null && !isHideAdornments()) {
				str += getNameBuilderVisitor().injectionOnStart(this);
				str += getNameBuilderVisitor().injectionOnParentStart(this);
			}
		}
		else {
			if (getNameBuilderVisitor() != null && !isHideAdornments()) {
				str += getNameBuilderVisitor().injectionOnChildStart(this);
			}
		}
		if (!isHideAdornments()) {
			str += "(";
		}
		VWMLLinkIncrementalIterator it = getLink().acquireLinkedObjectsIterator();
		if (it != null) {
			for(SHLEntity le = (SHLEntity)getLink().peek(it); le != null; le = (SHLEntity)getLink().peek(it)) {
				le.setNameBuilderVisitor(getNameBuilderVisitor());
				if (le.isMarkedAsComplexEntity()) {
					str = ((SHLComplexEntity)le).prepareVWMLCode(str, false);
				}
				else {
					str = le.asVWMLCode(str, false) + (it.isCorrect() ? " " : "");
				}
				le.setNameBuilderVisitor(null);
			}
		}		
		if (firstTime) {
			if (getNameBuilderVisitor() != null && !isHideAdornments()) {
				str += getNameBuilderVisitor().injectionOnParentFinish(this);
				str += getNameBuilderVisitor().injectionOnFinish(this);
			}
		}
		else {
			if (getNameBuilderVisitor() != null && !isHideAdornments()) {
				str += getNameBuilderVisitor().injectionOnChildFinish(this);
			}
		}
		if (!isHideAdornments()) {
			str += ")";
		}
		else {
			str += "\r\n";
		}
		return str;
	}
}
