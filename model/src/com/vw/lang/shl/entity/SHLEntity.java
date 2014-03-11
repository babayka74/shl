package com.vw.lang.shl.entity;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.sink.java.VWMLObject;
import com.vw.lang.sink.java.link.VWMLLinkIncrementalIterator;
import com.vw.lang.sink.java.operations.VWMLOperation;
import com.vw.lang.sink.java.operations.VWMLOperations;

/**
 * Specific type of entity related to SHL language
 * conceptually can be considered as regular VWMLEntity, but has set of advanced properties
 * needed for building functionality of IDE 
 * @author Oleg
 *
 */
public class SHLEntity extends VWMLObject {

	private SHLEntity interpreting;
	private SHLContext context;
	private boolean isLifeTerm = false;
	private boolean isLifeTermAsSource = false;
	private VWMLOperations associatedOperations = new VWMLOperations("__associated_operation__" + this);
	
	public SHLEntity(Object hashId) {
		super(hashId);
	}
	
	public SHLEntity(SHLContext context, Object hashId, Object id, String readableId) {
		super(hashId, id, readableId);
		setContext(context);
	}

	public SHLContext getContext() {
		return context;
	}

	public void setContext(SHLContext context) {
		this.context = context;
	}
	
	public boolean isMarkedAsComplexEntity() {
		return false;
	}
	
	/**
	 * Adds operation to set of associative operations
	 * @param op
	 */
	public void addOperation(VWMLOperation op) {
		associatedOperations.addOperation(op);
		if (this.getLink().getLinkOperationVisitor() != null) {
			this.getLink().getLinkOperationVisitor().associateOperation(this, op);
		}
	}
	
	/**
	 * Removes operation from set of associative operations
	 * @param op
	 */
	public void removeOperation(VWMLOperation op) {
		associatedOperations.removeOperation(op);
		if (this.getLink().getLinkOperationVisitor() != null) {
			this.getLink().getLinkOperationVisitor().removeOperationFromAssociation(this, op);
		}
	}
	
	/**
	 * Returns operation and moves pointer to next operation if such exists
	 * @return
	 */
	public VWMLOperation getOperation(VWMLLinkIncrementalIterator it) {
		return associatedOperations.peekOperation(it);
	}
	
	/**
	 * Returns instance of iterator of container of operations objects
	 * @return
	 */
	public VWMLLinkIncrementalIterator acquireOperationsIterator() {
		VWMLLinkIncrementalIterator it = null;
		if (associatedOperations.operations() != 0) {
			it = new VWMLLinkIncrementalIterator(associatedOperations.operations());
		}
		return it;
	}
	
	public boolean isLifeTerm() {
		return isLifeTerm;
	}

	public void setLifeTerm(boolean isLifeTerm) {
		this.isLifeTerm = isLifeTerm;
	}
	
	public boolean isLifeTermAsSource() {
		return isLifeTermAsSource;
	}

	public void setLifeTermAsSource(boolean isLifeTermAsSource) {
		this.isLifeTermAsSource = isLifeTermAsSource;
		setLifeTerm(true);
	}

	public SHLEntity getInterpreting() {
		return interpreting;
	}

	public void setInterpreting(SHLEntity interpreting) {
		this.interpreting = interpreting;
	}

	/**
	 * Returns true in case if entity can be considered as 'term'
	 * @return
	 */
	public boolean isTerm() {
		return (associatedOperations.operations() != 0) ? true : false;
	}
	
	@Override
	public String buildReadableId() {
		return getReadableId();
	}
}
