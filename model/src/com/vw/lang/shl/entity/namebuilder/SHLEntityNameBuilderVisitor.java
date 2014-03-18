package com.vw.lang.shl.entity.namebuilder;

import com.vw.lang.generator.utils.Utils;
import com.vw.lang.sink.utils.IEntityNameBuilderVisitor;

public class SHLEntityNameBuilderVisitor implements IEntityNameBuilderVisitor {

	private int startingDelimIndex = 0;
	
	public int getStartingDelimIndex() {
		return startingDelimIndex;
	}

	public void setStartingDelimIndex(int startingDelimIndex) {
		this.startingDelimIndex = startingDelimIndex;
	}

	@Override
	public String injectionOnStart() {
		return null;
	}

	@Override
	public String injectionOnFinish() {
		return Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex) + "\r\n";
	}

	@Override
	public String injectionOnParentStart() {
		return null;
	}

	@Override
	public String injectionOnParentFinish() {
		return null;
	}

	@Override
	public String injectionOnChildStart() {
		startingDelimIndex++;
		return "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex);
	}

	@Override
	public String injectionOnChildFinish() {
		String s = "\r\n" + Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), startingDelimIndex);
		startingDelimIndex--;
		return s;
	}
}
