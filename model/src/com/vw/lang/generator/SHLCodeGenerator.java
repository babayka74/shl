package com.vw.lang.generator;

import org.apache.log4j.Logger;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.shl.entity.SHLEntityBuilder;
import com.vw.lang.shl.repositories.SHLContextsRepository;
import com.vw.lang.sink.ICodeGenerator;
import com.vw.lang.sink.java.link.AbstractVWMLLinkVisitor;
import com.vw.lang.sink.java.operations.VWMLOperation;
import com.vw.lang.sink.java.operations.VWMLOperationsCode;
import com.vw.lang.sink.utils.EntityWalker;

/**
 * 
 * SHL to VWML code generator
 * @author Oleg
 *
 */
public class SHLCodeGenerator implements ICodeGenerator {

	public static class CodeGeneratorProps  {
		private int offset;

		public CodeGeneratorProps(int offset) {
			super();
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}
	}
	
	public static class FrameCodeGeneratorProps extends CodeGeneratorProps {
		private Object frameId;

		public FrameCodeGeneratorProps(int offset, Object frameId) {
			super(offset);
			this.frameId = frameId;
		}

		public Object getFrameId() {
			return frameId;
		}

		public void setFrameId(Object frameId) {
			this.frameId = frameId;
		}
	}
	
	public static class ShlModuleStartProps extends StartModuleProps {
		
		public static enum GENERATE_CODE_FOR {
			NONE, FRAME, SCHEMA
		}
		
		private String vwmlSrcPath;
		private String vwmlModuleName;
		private FrameCodeGeneratorProps frameCodeGeneratorProps = null;
		private GENERATE_CODE_FOR generateCodeFor = GENERATE_CODE_FOR.NONE;

		public static FrameCodeGeneratorProps buildFrameCodeGeneratorProps(int offset, Object frameId) {
			return new FrameCodeGeneratorProps(offset, frameId);
		}
		
		public String getVwmlSrcPath() {
			return vwmlSrcPath;
		}

		public void setVwmlSrcPath(String vwmlSrcPath) {
			this.vwmlSrcPath = vwmlSrcPath;
		}

		public String getVwmlModuleName() {
			return vwmlModuleName;
		}

		public void setVwmlModuleName(String vwmlModuleName) {
			this.vwmlModuleName = vwmlModuleName;
		}

		public FrameCodeGeneratorProps getFrameCodeGeneratorProps() {
			return frameCodeGeneratorProps;
		}

		public void setFrameCodeGeneratorProps(FrameCodeGeneratorProps frameCodeGeneratorProps) {
			this.frameCodeGeneratorProps = frameCodeGeneratorProps;
		}

		public GENERATE_CODE_FOR getGenerateCodeFor() {
			return generateCodeFor;
		}

		public void setGenerateCodeFor(GENERATE_CODE_FOR generateCodeFor) {
			this.generateCodeFor = generateCodeFor;
		}
	}
	private SHLContextsRepository shlContextRepository = SHLContextsRepository.instance();
	// internal logger
	private static Logger logger = Logger.getLogger(SHLCodeGenerator.class);
	
	@Override
	public Object getLastLink() {
		return null;
	}

	@Override
	public Object getLastLinksUniqId() {
		return null;
	}

	@Override
	public StartModuleProps buildProps() {
		ShlModuleStartProps props = new ShlModuleStartProps();
		props.setCodeGenerator(this);
		return props;
	}

	@Override
	public StartModuleProps normalizeProps(StartModuleProps props, StartModuleProps projectProps) {
		return props;
	}

	@Override
	public String getSourcePath(StartModuleProps props) {
		ShlModuleStartProps modProps = (ShlModuleStartProps)props;
		if (StartModuleProps.getSourcesPath() == null) {
			StartModuleProps.setSourcesPath(modProps.getVwmlSrcPath());
		}
		return modProps.getVwmlSrcPath();
	}

	@Override
	public void startModule(StartModuleProps props) throws Exception {
	}

	@Override
	public void generate(StartModuleProps props) throws Exception {
	}

	@Override
	public void finishModule(StartModuleProps props) throws Exception {
	}
	
	@Override
	public void markEntityAsTerm(Object id, String[] contexts) throws Exception {
	}

	@Override
	public void markEntityAsLifeTermOnContexts(Object id, boolean asSource, String[] contexts) throws Exception {
		EntityWalker.Relation rel = (EntityWalker.Relation)id;
		if (contexts == null) {
			throw new Exception("contexts couldn't be null");
		}
		for(String context : contexts) {
			SHLContext shlContext = acquireContext(context);
			SHLEntity entity = shlContext.findEntity(rel.getObj());
			if (entity == null) {
				throw new Exception("Couldn't find entity '" + id + "' on SHL context '" + shlContext.getContextName() + "'");
			}
			entity.setLifeTermAsSource(asSource);
			if (logger.isDebugEnabled()) {
				logger.debug("Entity '" + rel.getObj() + "' marked as lifeterm on context '" + context + "'; source is '" + asSource + "'");
			}
		}
	}
	
	@Override
	public void markEntityAsLifeTerm(Object id, boolean asSource) throws Exception {
		throw new Exception("not implemented for SHL");
	}

	@Override
	public void declareSimpleEntity(Object id, String context) throws Exception {
		SHLContext shlContext = acquireContext(context);
		SHLEntity entity = SHLEntityBuilder.buildSimpleEntity(id, shlContext.getContextName(), shlContext);
		shlContext.associateEntity(entity);
		if (logger.isDebugEnabled()) {
			logger.debug("Simple entity '" + id + "' decalred on context '" + context + "'");
		}
	}

	@Override
	public void declareComplexEntity(Object id, Object readableId, String context) throws Exception {
		SHLContext shlContext = acquireContext(context);
		SHLEntity entity = SHLEntityBuilder.buildComplexEntity(id, shlContext.getContextName(), shlContext);
		entity.setReadableId((String)readableId);
		shlContext.associateEntity(entity);
		if (logger.isDebugEnabled()) {
			logger.debug("Complex entity '" + id + "' decalred on context '" + context + "'");
		}
	}

	@Override
	public void declareCreature(Object id, Object props, String context) throws Exception {
		throw new Exception("not implemented for SHL language");
	}

	@Override
	public boolean removeComplexEntityFromDeclarationAndLinkage(Object id, String[] contexts) {
		EntityWalker.Relation rel = (EntityWalker.Relation)id;
		for(String context : contexts) {
			SHLContext shlContext = acquireContext(context);
			shlContext.unAssociateEntity(rel.getObj());
			if (logger.isDebugEnabled()) {
				logger.debug("Entity '" + rel.getObj() + "' removed from context '" + context + "'");
			}
		}
		return true;
	}

	@Override
	public void changeObjectIdTo(Object id, Object idTo, String[] contexts) {
	}

	@Override
	public void changeObjectIdToImmidiatly(Object id, Object idTo, String[] contexts) {
		for(String context : contexts) {
			SHLContext shlContext = acquireContext(context);
			SHLEntity entity = shlContext.findEntity(id);
			if (entity != null) {
				entity.setId(idTo);
				entity.buildReadableId();
				if (logger.isDebugEnabled()) {
					logger.debug("Entity '" + id + "' changed to '" + idTo + "' on context '" + context + "'");
				}
			}
		}
	}

	@Override
	public void changeObjectIdToForDeclaredObjectsOnly(Object id, Object idTo, String[] contexts) {
	}

	@Override
	public void declareTerm(Object id, String context) throws Exception {
		throw new Exception("unimplemented for SHL");
	}

	@Override
	public void declareContext(Object contextId) {
		shlContextRepository.createContextIfNotExists(contextId);
	}

	@Override
	public void linkObjects(Object id, Object linkedObjId, String linkingContext, String activeContext, Object uniqId) {
		SHLContext shlContext = acquireContext(linkingContext);
		SHLEntity entityLinking = shlContext.findEntity(id);
		SHLEntity entityLinked = shlContext.findEntity(linkedObjId);
		if (entityLinking != null && entityLinked != null) {
			entityLinking.link(entityLinked);
		}
	}

	@Override
	public void associateOperation(Object id, String op, String activeContext) {
		SHLContext shlContext = acquireContext(activeContext);
		SHLEntity entity = shlContext.findEntity(id);
		if (entity != null) {
			entity.addOperation(new VWMLOperation(VWMLOperationsCode.fromValue(op)));
		}
	}

	@Override
	public void interpretObjects(Object id, Object interpretingObjId, String linkingContext, String activeContext) {
		SHLContext shlContext = acquireContext(activeContext);
		SHLEntity entityInterpreted = shlContext.findEntity(id);
		SHLEntity entityInterpreting = shlContext.findEntity(interpretingObjId);
		if (entityInterpreted != null && entityInterpreting != null) {
			entityInterpreted.setInterpreting(entityInterpreting);
		}
	}

	@Override
	public AbstractVWMLLinkVisitor getVisitor() {
		return null;
	}

	@Override
	public void setVisitor(AbstractVWMLLinkVisitor visitor) {
	}

	@Override
	public String getLangAsString() {
		return "SHL";
	}

	@Override
	public void startConflictDefinitionOnRing(String conflictDefinitionName) throws Exception {
		throw new Exception("Not implemented for SHL language");
	}

	@Override
	public void addConflictDefinitionOnRing(String conflictDefinitionName) throws Exception {
		throw new Exception("Not implemented for SHL language");
	}

	@Override
	public void endConflictDefinitionOnRing() throws Exception {
		throw new Exception("Not implemented for SHL language");
	}
	
	private SHLContext acquireContext(Object contextId) {
		SHLContext shlContext = shlContextRepository.get(contextId);
		if (shlContext == null) {
			shlContextRepository.createContextIfNotExists(contextId);
		}
		return shlContext;
	}
}
