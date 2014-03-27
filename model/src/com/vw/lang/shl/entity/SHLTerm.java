package com.vw.lang.shl.entity;

import com.vw.lang.generator.utils.Utils;
import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.sink.java.link.VWMLLinkIncrementalIterator;
import com.vw.lang.sink.java.operations.VWMLOperation;
import com.vw.lang.sink.java.operations.VWMLOperations;
import com.vw.lang.sink.java.operations.VWMLOperationsCode;

public class SHLTerm extends SHLEntity {

	/**
	 * Following properties used when term's vwml code is generated
	 * @author Oleg
	 *
	 */
	protected static class PropsForVWMLCode {
		private String suffixVWMLCode = "";
		private String prefixVWMLCode = "";
		
		public String getSuffixVWMLCode() {
			return suffixVWMLCode;
		}
		
		public void setSuffixVWMLCode(String suffixVWMLCode) {
			this.suffixVWMLCode = suffixVWMLCode;
		}
		
		public String getPrefixVWMLCode() {
			return prefixVWMLCode;
		}

		public void setPrefixVWMLCode(String prefixVWMLCode) {
			this.prefixVWMLCode = prefixVWMLCode;
		}
	}
	
	private static VWMLOperation s_dynContextOp = new VWMLOperation(VWMLOperationsCode.OPDYNCONTEXT);
	private static VWMLOperation lastOperation = null;

	private SHLEntity linkedEntity = null;

	public static VWMLOperation getLastOperation() {
		return lastOperation;
	}
	
	public static void resetLastOperation() {
		lastOperation = null;
	}
	
	public SHLTerm(Object hashId) {
		super(hashId);
	}
	
	public SHLTerm(SHLContext context, Object hashId, Object id, String readableId) {
		super(context, hashId, id, readableId);
	}
	
	public SHLEntity getLinkedEntity() {
		return linkedEntity;
	}

	public void setLinkedEntity(SHLEntity linkedEntity) {
		if (linkedEntity != null) {
			this.linkedEntity = linkedEntity;
			setReadableId(linkedEntity.getReadableId());
		}
	}

	@Override
	public String buildReadableId() {
		if (linkedEntity == null) {
			return "__unknown__";
		}
		if (linkedEntity.getReadableId() == null) {
			linkedEntity.setReadableId((String)getId());
		}
		return linkedEntity.getReadableId();
	}

	@Override
	public String asVWMLCode(String prefix, boolean start) {
		VWMLOperations ops = linkedEntity.getAssociatedOperations();
		linkedEntity.setNameBuilderVisitor(getNameBuilderVisitor());
		linkedEntity.setAssociatedOperations(getAssociatedOperations());
		PropsForVWMLCode props = applyFromattingStrategy();
		String vwmlCode = prefix +
						  props.getPrefixVWMLCode() +
						  linkedEntity.asVWMLCode("", start) +
						  getOperationsAsVWMLCode() +
						  props.getSuffixVWMLCode();
		linkedEntity.setAssociatedOperations(ops);
		linkedEntity.setNameBuilderVisitor(null);
		return vwmlCode;
	}

	public String getOperationsAsVWMLCode() {
		String ops = " ";
		VWMLLinkIncrementalIterator it = getAssociatedOperations().acquireLinkedObjectsIterator();
		if (getAssociatedOperations().operations() == 1) {
			VWMLOperation op = getAssociatedOperations().peekOperation(it);
			if (isOperationInSpecialList(op)) {
				ops = op.getOpCode().toValue();
			}
			else {
				ops += op.getOpCode().toValue() + " ";
			}
			lastOperation = op;
		}
		else {
			while(it.isCorrect()) {
				VWMLOperation op = getAssociatedOperations().peekOperation(it);
				ops += op.getOpCode().toValue();
				if (!isOperationInSpecialList(op)) {
					ops += (it.isCorrect() ? " " : "");
				}
				lastOperation = op;
			}
		}
		return ops;
	}
	
	protected boolean isOperationInSpecialList(VWMLOperation op) {
		VWMLOperationsCode[] codes = { VWMLOperationsCode.OPINTERPRET,
									   VWMLOperationsCode.OPDYNCONTEXT,
									   VWMLOperationsCode.OPCREATEEXPR
									 };
		for(VWMLOperationsCode code : codes) {
			if (op.getOpCode() == code) {
				return true;
			}
		}
		return false;
	}
	
	protected PropsForVWMLCode applyFromattingStrategy() {
		String prefix = "";
		String sourceLifeTermPrefix = "";
		PropsForVWMLCode p = new PropsForVWMLCode();
		if (linkedEntity.isLifeTerm()) {
			sourceLifeTermPrefix = "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), getNameBuilderVisitor().getStartingDelimIndex() + 1);
			if (linkedEntity.isLifeTermAsSource()) {
				sourceLifeTermPrefix += "source lifeterm = ";
			}
			else {
				sourceLifeTermPrefix += "lifeterm = ";
			}
			prefix = sourceLifeTermPrefix;
			getNameBuilderVisitor().forceStartingDelimIndex(getNameBuilderVisitor().getStartingDelimIndex() + 1);
		}
		if (!linkedEntity.isMarkedAsComplexEntity()) {
			VWMLLinkIncrementalIterator it = getAssociatedOperations().acquireLinkedObjectsIterator();
			it.setIt(getAssociatedOperations().operations() - 1);
			VWMLOperation op = getAssociatedOperations().peekOperation(it);
			if (op.getOpCode() == VWMLOperationsCode.OPEXECUTE) {
				prefix = sourceLifeTermPrefix + 
						 "\r\n" + 
						 Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), getNameBuilderVisitor().getStartingDelimIndex() + 1);
			}
		}
		p.setPrefixVWMLCode(prefix);
		return p;
	}
}
