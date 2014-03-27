package com.vw.lang.shl.entity.namebuilder;

import com.vw.lang.generator.utils.Utils;
import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.sink.java.VWMLObject;
import com.vw.lang.sink.java.operations.VWMLOperation;
import com.vw.lang.sink.java.operations.VWMLOperationsCode;
import com.vw.lang.sink.utils.IEntityNameBuilderVisitor;

public class SHLEntityNameBuilderVisitor implements IEntityNameBuilderVisitor {

	private int startingDelimIndex = 0;
	private static String s_nothingToAdd = "";
	
	public void forceStartingDelimIndex(int index) {
		startingDelimIndex = index;
	}
	
	public int getStartingDelimIndex() {
		return startingDelimIndex;
	}

	public void setStartingDelimIndex(int startingDelimIndex) {
		this.startingDelimIndex = startingDelimIndex;
	}

	@Override
	public String injectionOnStart(VWMLObject e) {
		return s_nothingToAdd;
	}

	@Override
	public String injectionOnFinish(VWMLObject e) {
		return s_nothingToAdd;
	}

	@Override
	public String injectionOnParentStart(VWMLObject e) {
		return s_nothingToAdd;
	}

	@Override
	public String injectionOnParentFinish(VWMLObject e) {
		String s = s_nothingToAdd;
		SHLEntity se = (SHLEntity)e;
		if (!(se.isTerm() && se.operationInList(new VWMLOperation(VWMLOperationsCode.OPDYNCONTEXT)))) {
			s = "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex);
		}
		startingDelimIndex--;
		return s;
	}

	@Override
	public String injectionOnChildStart(VWMLObject e) {
		SHLEntity se = (SHLEntity)e;
		startingDelimIndex++;
		if (!(se.isTerm() && se.operationInList(new VWMLOperation(VWMLOperationsCode.OPDYNCONTEXT)))) {
			return "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex);
		}
		return s_nothingToAdd;
	}

	@Override
	public String injectionOnChildFinish(VWMLObject e) {
		String s = s_nothingToAdd;
		SHLEntity se = (SHLEntity)e;
		if (!(se.isTerm() && se.operationInList(new VWMLOperation(VWMLOperationsCode.OPDYNCONTEXT)))) {
			s = "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex);
		}
		startingDelimIndex--;
		return s;
	}
}
