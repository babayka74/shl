package com.vw.lang.generator.frame;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;

/**
 * Abstract class which is used for converting SHL -> VWML data structures into readable form
 * @author Oleg
 *
 */
public abstract class SHL2VWMLCodeGeneratorWriter {

	/**
	 * Initializes writer
	 * @param url
	 * @throws Exception
	 */
	public abstract void init(String url) throws Exception;

	/**
	 * Called when writer finishes its operations and should be closed
	 * @throws Exception
	 */
	public abstract void done() throws Exception;
	
	/**
	 * Flushes generated code
	 * @throws Exception
	 */
	public abstract void flush() throws Exception;
	
	/**
	 * Starts VWML context
	 * @param context
	 * @param delimIndex
	 * @throws Exception
	 */
	public abstract void writeContextBegin(SHLContext context, int delimIndex) throws Exception;

	/**
	 * Finishes VWML context
	 * @param context
	 * @param delimIndex
	 * @throws Exception
	 */
	public abstract void writeContextEnd(SHLContext context, int delimIndex) throws Exception;
	
	/**
	 * Writes VWML entity
	 * @param context
	 * @param entity
	 * @param delimIndex
	 * @throws Exception
	 */
	public abstract void writeEntity(SHLContext context, SHLEntity entity, int delimIndex) throws Exception;	
}
