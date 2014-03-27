package com.vw.lang.generator.writer.string;

import java.util.HashMap;
import java.util.Map;

import com.vw.lang.generator.frame.SHL2VWMLCodeGeneratorWriter;
import com.vw.lang.generator.utils.Utils;
import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;

/**
 * SHL -> VWML string
 * @author Oleg
 *
 */
public class SHL2VWMLCodeGeneratorStringWriter extends SHL2VWMLCodeGeneratorWriter {

	private Map<Integer, String> contextsCodeByIndex = new HashMap<Integer, String>();
	private String contextCode = "";
	private StringBuffer code = new StringBuffer();
	
	@Override
	public void init(String url) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws Exception {
		System.out.println(code);
	}
	
	@Override
	public void writeContextBegin(SHLContext context, int delimIndex) throws Exception {
		String ctxAsStr = Utils.getContextsActualName(context);
		String offset = Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), delimIndex);
		contextCode += "\r\n" + offset + ctxAsStr + " ias ";
		if (context.getLink().getLinkedObjectsOnThisTime() != 0) { 
			contextCode += "(";
		}
	}

	@Override
	public void writeContextEnd(SHLContext context, int delimIndex) throws Exception {
		String offset = Utils.generateStrOffsetPattern(Utils.getOffsetPattern(), delimIndex);
		String suffix = "";
		if (context.getLink().getLinkedObjectsOnThisTime() != 0) { 
			suffix = offset + ")";
		}
		contextCode += suffix + ";" + "\r\n";
		contextsCodeByIndex.put(delimIndex, contextCode);
		code.append(contextCode);
		contextCode = "";
	}

	@Override
	public void writeEntity(SHLContext context, SHLEntity entity, int delimIndex) throws Exception {
		String entityAsStr = entity.asVWMLCode("", true);
		contextCode += entityAsStr;
	}
}
