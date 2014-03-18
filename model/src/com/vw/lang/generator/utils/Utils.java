package com.vw.lang.generator.utils;

import com.vw.lang.processor.context.builder.VWMLContextBuilder;
import com.vw.lang.shl.context.SHLContext;

/**
 * Some independent methods
 * @author Oleg
 *
 */
public class Utils {

	private static String s_offsetPattern = "    ";
	
	/**
	 * Generates offset string which contains 'tabs'
	 * @param offsetPattern
	 * @param delimIndex
	 * @return
	 */
	public static String generateStrOffsetPattern(String offsetPattern, int delimIndex) {
		String r = "";
		for(int i = 0; i < delimIndex; i++) {
			r += offsetPattern;
		}
		return r;
	}
	
	/**
	 * Builds context's actual name
	 * @param ctx
	 * @return
	 */
	public static String getContextsActualName(SHLContext ctx) {
		String ctxAsStr = null;
		VWMLContextBuilder.ContextBunch bunch = ctx.getAssociatedBunch();
		if (bunch != null) {
			ctxAsStr = bunch.bunchAsString();
		}
		else {
			ctxAsStr = ctx.getContextName();
		}
		return ctxAsStr;
	}
	
	public static String getOffsetPattern() {
		return s_offsetPattern;
	}
	
}
