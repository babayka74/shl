package com.vw.lang.generator.frame;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vw.lang.generator.VWMLCodeGenerator;
import com.vw.lang.generator.VWMLCodeGenerator.ShlModuleStartProps;
import com.vw.lang.generator.utils.Utils;
import com.vw.lang.processor.context.builder.VWMLContextBuilder;
import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.shl.entity.namebuilder.SHLEntityNameBuilderVisitor;
import com.vw.lang.shl.repositories.SHLContextsRepository;
import com.vw.lang.sink.java.VWMLObject;

/**
 * Generates VWML code from SHL frame
 * @author Oleg
 *
 */
public class SHL2VWMLFrameCodeGenerator {
	
	protected static class SHLContextProcessingDescriptor {
		// starting offset during code generation phase
		private int delimIndex;
		private SHLContext context;
		
		public SHLContextProcessingDescriptor(int delimIndex, SHLContext context) {
			super();
			this.delimIndex = delimIndex;
			this.context = context;
		}

		public int getDelimIndex() {
			return delimIndex;
		}

		public void setDelimIndex(int delimIndex) {
			this.delimIndex = delimIndex;
		}

		public SHLContext getContext() {
			return context;
		}

		public void setContext(SHLContext context) {
			this.context = context;
		}
	}
	
	private VWMLCodeGenerator codeGenerator = null;
	private SHLEntityNameBuilderVisitor nameBuilderVisitor = null;
	private static Logger logger = Logger.getLogger(SHL2VWMLFrameCodeGenerator.class);

	private SHL2VWMLFrameCodeGenerator(VWMLCodeGenerator codeGenerator) {
		this.codeGenerator = codeGenerator;
	}
	
	public static SHL2VWMLFrameCodeGenerator instance(VWMLCodeGenerator codeGenerator) {
		return new SHL2VWMLFrameCodeGenerator(codeGenerator);
	}
	
	public VWMLCodeGenerator getCodeGenerator() {
		return codeGenerator;
	}

	public void setCodeGenerator(VWMLCodeGenerator codeGenerator) {
		this.codeGenerator = codeGenerator;
	}
	
	/**
	 * Generates VWML code by SHL; the generation is started from given frame 
	 * name which is set inside generator's properties
	 * @param props
	 */
	public void generate(ShlModuleStartProps props) {
		nameBuilderVisitor = new SHLEntityNameBuilderVisitor();
		nameBuilderVisitor.setStartingDelimIndex(0);
		String generateFromFrame = props.getFrameCodeGeneratorProps().getGenerateFromFrame();
		SHLContextsRepository shlContexts = codeGenerator.getShlContextRepository();
		SHLContext ctx = shlContexts.get(generateFromFrame);
		try {
			generateVWMLCode(ctx, props, 0, new HashSet<VWMLContextBuilder.ContextBunch>());
			props.getWriter().flush();
		} catch (Exception e) {
			logger.error("Exception caught during generation of VWML code; frame '" + generateFromFrame + "'");
			e.printStackTrace();
		}
	}
	
	protected void generateVWMLCode(SHLContext ctx, ShlModuleStartProps props, int delimIndex, Set<VWMLContextBuilder.ContextBunch> auxCache) throws Exception {
		if (ctx.getAssociatedBunch() != null && auxCache.contains(ctx.getAssociatedBunch())) {
			return; // has already been processed; it may happen in case if context belongs to bunch which has already been processed
		}
		if (ctx.getAssociatedBunch() != null) {
			auxCache.add(ctx.getAssociatedBunch());
		}
		startContext(ctx, props, delimIndex);
		for(VWMLObject objAsCtx : ctx.getLink().getLinkedObjects()) {
			generateVWMLCode((SHLContext)objAsCtx, props, delimIndex + 1, auxCache);
		}
		entity(ctx, props, delimIndex);
		endContext(ctx, props, delimIndex);
	}
	
	protected void startContext(SHLContext ctx, ShlModuleStartProps props, int delimIndex) throws Exception {
		if (props.getWriter() != null) {
			props.getWriter().writeContextBegin(ctx, delimIndex);
		}
	}

	protected void endContext(SHLContext ctx, ShlModuleStartProps props, int delimIndex) throws Exception {
		if (props.getWriter() != null) {
			props.getWriter().writeContextEnd(ctx, delimIndex);
		}
	}
	
	protected void entity(SHLContext ctx, ShlModuleStartProps props, int delimIndex) throws Exception {
		String ctxAsStr = Utils.getContextsActualName(ctx);
		SHLEntity iasRelation = ctx.getIasRelation();
		if (iasRelation == null) {
			throw new Exception("Context '" + ctxAsStr + "' doesn't have IAS relation");
		}
		nameBuilderVisitor.setStartingDelimIndex(delimIndex);
		iasRelation.setNameBuilderVisitor(nameBuilderVisitor);
		if (props.getWriter() != null) {
			if (!(ctx.getLink().getLinkedObjectsOnThisTime() != 0 && iasRelation.getLink().getLinkedObjectsOnThisTime() == 0)) {
				if (ctx.getLink().getLinkedObjectsOnThisTime() != 0) {
					iasRelation.setHideAdornments(true);
				}
				props.getWriter().writeEntity(ctx, iasRelation, delimIndex + 1);
				iasRelation.setHideAdornments(false);
			}
		}
	}	
}
