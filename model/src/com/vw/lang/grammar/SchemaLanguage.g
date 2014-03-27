
/**
---------------------- Schema language for developing Virtual World language  ------------------------------------

*/

grammar SchemaLanguage;
 
tokens {
    IAS='ias';
    LIFETERM='lifeterm';
    OPJOIN='Join';
    OPINTERSECT='Intersect';
    OPSUBSTRUCT='Substruct';
    OPFIRST='First';
    OPLAST='Last';
    OPBEGIN='Begin';
    OPREST='Rest';
    OPCARTESIAN='Cartesian';
    OPRANDOM='Random';
    OPIN='In';
    OPINCL='Include';
    OPEQ='Eq';
    OPIDENT='Ident';
    OPSQU='Squeeze';
    OPINTERPRET='~';
    OPCREATEEXPR= '^';
    OPEXECUTE='Exe';
    OPACTIVATECTX=':';
    OPACTIVATEONFRINGE='Do';
    OPRELAX='Relax';
    OPSTARTCONFLICTGROUP = '[';
    OPENDCONFLICTGROUP = ']';
    OPBREAKPOINT = 'Bp';
    OPAPPLYTOCONTEXT='Context';
    OPCLONE='Clone';   
    OPPROJECTION='Projection';
    OPFOREACH='ForEach';
    OPDYNCONTEXT='->';
    OPSIZE='Size';
    OPINTERRUPT='Interrupt';
    
    // languages
    JAVA='__java__';
    C='__c__';
    CPP='__cpp__';
    OBJECTIVEC='__objective_c__';
}

@header {
package com.vw.lang.grammar;

// Exceptions
import java.lang.Throwable;

// general code generator's imports
import com.vw.lang.sink.ICodeGenerator;
import com.vw.lang.sink.ICodeGenerator.StartModuleProps;
import com.vw.lang.sink.utils.ComplexEntityNameBuilder;
import com.vw.lang.sink.utils.EntityWalker;
import com.vw.lang.sink.utils.EntityWalker.EntityDescriptor;
import com.vw.lang.sink.utils.EntityWalker.ComplexContextDescriptor;
import com.vw.lang.sink.utils.GeneralUtils;

import com.vw.lang.processor.context.builder.VWMLContextBuilder;
import com.vw.lang.processor.context.builder.VWMLContextBuilder.ContextBunch;
import com.vw.lang.processor.context.builder.VWMLContextBuilder.ContextBunchElement;

import com.vw.lang.processor.model.builder.SHLModelBuilder;
import com.vw.lang.generator.VWMLCodeGenerator.ShlModuleStartProps;

// logger
import org.apache.log4j.Logger;

}

@lexer::header { 
package com.vw.lang.grammar;
}

@lexer::members {
        private static final int NATIVE_CODE_CHANNEL = 199;
}

@rulecatch {
    catch (RecognitionException e) {
        throw e;
    }
}


@members {

	public static class SHLCodeGeneratorRecognitionException extends RecognitionException {
		public SHLCodeGeneratorRecognitionException() {
			super();
		}
		
		public SHLCodeGeneratorRecognitionException(String message) {
			initCause(new Throwable(message));
		}
	}

	private boolean moduleInProgress = false;
	private boolean lastProcessedEntityAsTerm = false;
	private boolean sourceLifeTermDetectedFlag = false;
	private EntityWalker.Relation lastProcessedEntity = null;
	private EntityWalker contextWalker = EntityWalker.instance();
	private EntityWalker entityWalker = EntityWalker.instance();
	private String lastProcessedComplexEntityId = null;	
	private ShlModuleStartProps modProps = null;
	private String currentFrameName = null;
	private String rootFrameName = null;
	// VWML exported
	private VWMLContextBuilder vwmlContextBuilder = VWMLContextBuilder.instance();
	private VWMLContextBuilder.ContextBunch lastProcessedContextBunch = null;
	// for entity declaration (aka context)
	private ComplexEntityNameBuilder complexEntityNameBuilderDecl = ComplexEntityNameBuilder.instance();
	// for regular entity which is used inside expression
	private ComplexEntityNameBuilder complexEntityNameBuilderDef = null;

 	private Logger logger = Logger.getLogger(this.getClass());

	private static SHLModelBuilder shlModelBuilder = SHLModelBuilder.instance();
	private static ICodeGenerator codeGenerator = null;
	private static int includeDepth = 0;
	
	public String getRootFrameName() {
		return rootFrameName;
	}

	public void setRootFrameName(String rootFrameName) {
		this.rootFrameName = rootFrameName;
	}
	
	public String getCurrentFrameName() {
		return currentFrameName;
	}
	
	public void setCurrentFrameName(String name) {
		currentFrameName = name;
	}
	
	public StartModuleProps getModuleProps() {
		return modProps;
	}
	
	public void setModuleProps(StartModuleProps modProps) {
		this.modProps = (ShlModuleStartProps)modProps;
	}
	
	protected void processInclude(String file) throws RecognitionException {
		try {
			includeDepth++;
			includeDepth--;
		}
		catch(Exception e) {
			rethrowSHLExceptionAsRecognitionException(e);
		}
	} 

	protected void rethrowSHLExceptionAsRecognitionException(Exception e) throws RecognitionException {
		throw new SHLCodeGeneratorRecognitionException(e.getMessage());
	}
	
	protected String simpleEntityDeclaration(String id) throws RecognitionException {
    		// means that complex entity's name is being built
    		if (complexEntityNameBuilderDecl.isInProgress()) {
    		        if (logger.isDebugEnabled()) {
    		        	logger.debug("simple entity '" + id + "' is added as part of complex entity");
    		        }
    			complexEntityNameBuilderDecl.addObjectId(id);
    		}
    		else {
    			try {
    				VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
    				if (codeGenerator != null) {
    					for(String c = contexts.first(); c != null; c = contexts.next()) {
    						codeGenerator.declareSimpleEntity(id, c);
     		        			if (logger.isDebugEnabled()) {
    		        				logger.debug("simple entity '" + id + "' is declared; context '" + c + "'");
    		        			}
    					}
    				}
    			}
    			catch(Exception e) {
    				rethrowSHLExceptionAsRecognitionException(e);
    			}
    		}
    		return id;
	}
	
	protected void complexEntityDeclarationPhase1() throws RecognitionException {
    	    	complexEntityNameBuilderDecl.startProgress();
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("complex entity declaration process - started");
	    	}	
	}
	
	protected void complexEntityDeclarationPhase2() throws RecognitionException {
		complexEntityNameBuilderDecl.stopProgress();
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("complex entity declaration process - stopped");
	    	}			
	}
	
	protected String complexEntityDeclarationPhase3() throws RecognitionException {
              	String id = complexEntityNameBuilderDecl.build();
              	complexEntityNameBuilderDecl.clear();
		try {
			VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
			if (codeGenerator != null) {
				for(String c = contexts.first(); c != null; c = contexts.next()) {
					codeGenerator.declareComplexEntity(id, null, c);
				}
			}    	
			if (logger.isDebugEnabled()) {
				logger.debug("complex entity '" + id + "' is declared; contexts '" + contexts + "'");
				logger.debug("complex entity declaration process - finished");
			}    	
		}
		catch(Exception e) {
			rethrowSHLExceptionAsRecognitionException(e);
		}
		return id;            					
	}

	protected void createLastProcessedContextBunch() {
    		lastProcessedContextBunch = VWMLContextBuilder.ContextBunch.instance();
    		if (logger.isDebugEnabled()) {
    			logger.debug("Created bunch");
    		}
	}

	protected void addBunchElementToLastProcessedContextBunch(Object id) {
    		lastProcessedContextBunch.add(ContextBunchElement.build(id));
    		if (logger.isDebugEnabled()) {
    			logger.debug("Bunch element '" + id + "'");
    		}	
	}

	protected void handleLastProcessedContextBunch() {
    		VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
        	vwmlContextBuilder.push(lastProcessedContextBunch);
        	if (logger.isDebugEnabled()) {
        		logger.debug("Pushed '" + lastProcessedContextBunch + "'; parent contexts '" + contexts + "'");
        	}	
	}

	protected void declareAbsoluteContextByIASRelation() throws RecognitionException {
		// point to check deffered actions on effective context
		unwindEffectiveContext();
    		// get context's bunch from stack
    		Object bunch = vwmlContextBuilder.peek();
    		entityWalker.markFutureEntityAsIAS(bunch);
    		if (logger.isDebugEnabled()) {
    			logger.debug("Context bunch '" + bunch + "' was marked as IAS - pushed to stack");
    		}
    		if (codeGenerator != null) {
    			VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
    			for(String c = contexts.first(); c != null; c = contexts.next()) {
    				if (logger.isDebugEnabled()) {
    					logger.debug("Context '" + c + "' is going to be declared");
    				}    				
    				codeGenerator.declareContext(c);
    				codeGenerator.linkContextAndBunch(c, bunch);
    				if (logger.isDebugEnabled()) {
    					logger.debug("Context '" + c + "' was declared");
    				}
    			}
    		}
	}

	protected void handleProcessedAbsoluteContextbyIASRelation() {
    		Object bunch = vwmlContextBuilder.peek();
    		if (lastProcessedEntity != null) {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Context bunch '" + bunch + "' which was marked as IAS - removed from context builder stack");
    			}
    		      	vwmlContextBuilder.pop();
    			if (logger.isDebugEnabled()) {
    				logger.debug("!!!!! '" + vwmlContextBuilder.peek() + "'");
    			}    		      	    		      	
    		}
    		else {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Context bunch '" + bunch + "' which was marked as IAS - stayed at context builder stack");
    			}
    		}
	}

	protected String unwindEffectiveContext() throws RecognitionException {
		ComplexContextDescriptor contextDescriptor = (ComplexContextDescriptor)contextWalker.peek();
		EntityWalker.EntityDescriptor entityDescr = null;
		String c = "";
		// top pushed entity's id should be changed on updated in case if its id is the same
		EntityWalker.Relation rel = (EntityWalker.Relation)entityWalker.peek();		
		if (contextDescriptor != null && logger.isDebugEnabled()) {
			logger.debug("Starting unwinding process of defferred effective context; top pushed entity is '" + rel.getObj() + "'");
		}
		while(contextDescriptor != null) {
			if (entityDescr == null) {
				entityDescr = (EntityWalker.EntityDescriptor)contextDescriptor.getUserData();
				if (logger.isDebugEnabled()) {
					logger.debug("creating effective context for entity descriptor '" + entityDescr + "'");
				}
			}
			// building context
			VWMLContextBuilder effectiveContextBuilder = (VWMLContextBuilder)contextDescriptor.getVwmlEffectiveContextBuilder();
			if (effectiveContextBuilder == null) {
				rethrowSHLExceptionAsRecognitionException(new Exception("'null' effective context builder encountered during effective context building operation"));
			}
			c = effectiveContextBuilder.getEffectiveContext() + "." + c;
			contextWalker.pop();
			contextDescriptor = (ComplexContextDescriptor)contextWalker.peek();
		}
		String newEntityId = null;
		if (entityDescr != null) {
			newEntityId = VWMLContextBuilder.buildFullEntityName(c, (String)entityDescr.getId());
			if (codeGenerator != null) {
				if (rel != null && rel.getObj().equals(entityDescr.getId())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Top pushed entity '" + entityDescr.getId() + "' is changed to '" + newEntityId + "' also");
					}
					rel.setObj(newEntityId);
				}
				codeGenerator.changeObjectIdToImmidiatly(entityDescr.getId(), newEntityId, (String[])entityDescr.getContexts());
				if (logger.isDebugEnabled()) {
					logger.debug("Entity '" + entityDescr.getId() + "' changed to '" + newEntityId + "'; contexts '" + entityDescr.getContexts() + "'");
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Finished unwinding process of defferred effective context");
			}
		}
		return newEntityId;
	}

	protected void addEffectiveContext(Object contextId) {
  		VWMLContextBuilder effectiveContextBuilder = null;
      		ComplexContextDescriptor contextDescriptor = (ComplexContextDescriptor)contextWalker.peek();
      		if (contextDescriptor != null) {
      			contextDescriptor.setAddressingByComplexContextEncountered(true);
      			contextDescriptor.setUserData(null);
      		}
    		effectiveContextBuilder = VWMLContextBuilder.instance();
   		// becomes part of 'complex context'
    		effectiveContextBuilder.addEffectiveContext((String)contextId);
    		// effective context on simple entity is equal '.' operator for complex entity
    		contextWalker.push(ComplexContextDescriptor.build(effectiveContextBuilder, false));
    		if (logger.isDebugEnabled()) {
    			logger.debug("effective context detected '" + contextId + "'");
    		}    			
	}

	protected void processComplexContext(EntityWalker.Relation rel) throws RecognitionException {
		if (codeGenerator != null) {
			// removes entity from declaration and linkage storage; entity is interpreted as 'complex context'
			String[] contexts = vwmlContextBuilder.buildContext().asStrings();
			codeGenerator.removeComplexEntityFromDeclarationAndLinkage(rel, contexts);
			if (logger.isDebugEnabled()) {
				logger.debug("entity '" + rel.getObj() + "' removed since it was recognized as complex context");
			}
			// throws relation to another entity... the relation is thrown until non-context entity is found
			if (EntityWalker.REL.ASSOCIATION == rel.getRelation()) {
    				Object fIAS = vwmlContextBuilder.peek();
    				if (logger.isDebugEnabled()) {
    					logger.debug("Object '" + fIAS + "' marked again as IAS");
    				}
    				entityWalker.markFutureEntityAsIAS(fIAS);
			}
   			// so entity is considered as effective context
   			if (lastProcessedComplexEntityId == null) {
				rethrowSHLExceptionAsRecognitionException(new Exception("invalid context; single context indicator '.' detected"));
   			}
   			if (logger.isDebugEnabled()) {
   				logger.debug("part of complex context '" + lastProcessedComplexEntityId + "' detected");
   			}
   			// adds effective context
   			addEffectiveContext(lastProcessedComplexEntityId);
		}
	}

	protected EntityWalker.Relation simpleEntityAssembling(String id) throws RecognitionException {
		EntityWalker.Relation rel = null;
		// since entity's id may include '.' we should check correctness of name
    		// if name ends with '.' we can suppouse that this name can be considered as complex entity's effective context
    		ComplexContextDescriptor contextDescriptor = (ComplexContextDescriptor)contextWalker.peek();
    		if (!VWMLContextBuilder.isEffectiveContext(id)) {
    			EntityWalker.Relation relParent = (EntityWalker.Relation)entityWalker.peek();
    			boolean partOfComplexEntity = false;
    			if (relParent != null) {
    				complexEntityNameBuilderDef = (ComplexEntityNameBuilder)relParent.getData();
	    			if (complexEntityNameBuilderDef.isInProgress()) {
	    				complexEntityNameBuilderDef.addObjectId(id);
	    				partOfComplexEntity = true;
	    			}
    			}
    			// standalone simple entity - chance to unwind defferred effective context
    			if (contextDescriptor != null && (relParent == null || !relParent.isParticipatesInComplexContextBuildingProcess())) {
    				if (contextDescriptor.getUserData() == null) {
    					String[] contexts = vwmlContextBuilder.buildContext().asStrings();
    					contextDescriptor.setUserData(EntityDescriptor.build(id, contexts));
					String newEntityId = unwindEffectiveContext();
					if (newEntityId != null) {
						id = newEntityId;
					}
				}
    			}    			
   			rel = buildRelation(id);
    			if (logger.isDebugEnabled()) {
    				logger.debug("processed simple entity '" + rel + "'");
    			}
    		}
    		else {
    			System.out.println("???????????????????????????????? " + id);
    			// effective context on simple entity is equal '.' operator for complex entity
   			// adds effective context
   			addEffectiveContext(id);
 			
    		}
		return rel;	
	}

	protected void complexEntityStartAssembling() throws RecognitionException {
    		// id and name is the same
    		complexEntityNameBuilderDef = ComplexEntityNameBuilder.instance();
    		complexEntityNameBuilderDef.startProgress();
    		String ceId = complexEntityNameBuilderDecl.generateRandomName();
    		boolean participatesInComplexContextBuildingProcess = false;
    		try {
    			if (codeGenerator != null) {
    				VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
    				ComplexContextDescriptor contextDescriptor = (ComplexContextDescriptor)contextWalker.peek();
    				if (contextDescriptor != null) {
    					if (contextDescriptor.getUserData() == null) {
    						String[] contextsAsStrings = vwmlContextBuilder.buildContext().asStrings();
    						contextDescriptor.setUserData(EntityDescriptor.build(ceId, contextsAsStrings));
    						participatesInComplexContextBuildingProcess = true;
    					}
					else {
						// next complex entity - chance to unwind deffreed effective context
						unwindEffectiveContext();
					}
    				}
    				for(String c = contexts.first(); c != null; c = contexts.next()) {
    					codeGenerator.declareComplexEntity(ceId, null, c);
    				}
    			}
    		}
    		catch(Exception e) {
    			rethrowSHLExceptionAsRecognitionException(e);
    		}
        	// the complex enity (name/id is generated) is pushed to stack (here complex entity is part of expression)
    		EntityWalker.Relation rel = buildRelation(ceId);
    		rel.setData(complexEntityNameBuilderDef);
    		rel.setParticipatesInComplexContextBuildingProcess(participatesInComplexContextBuildingProcess);
    		entityWalker.push(rel);
   		if (logger.isDebugEnabled()) {
   			logger.debug("complex entity '" + rel.getObj() + "' is declared");
   		}
	}
	
	protected EntityWalker.Relation complexEntityStopAssembling() throws RecognitionException {
        	// remove it from stack
    		EntityWalker.Relation rel = (EntityWalker.Relation)entityWalker.pop();
    		// builds complex entity readable name instead of generated
    		complexEntityNameBuilderDef = (ComplexEntityNameBuilder)rel.getData();
    		complexEntityNameBuilderDef.stopProgress();
        	lastProcessedComplexEntityId = complexEntityNameBuilderDef.build();
        	complexEntityNameBuilderDef.clear();    		
    		if (logger.isDebugEnabled()) {
    			logger.debug("processed complex entity '" + rel + "'");
    		}
    		return rel;
	}
	
	protected Object buildIASAssociation(Object id) throws RecognitionException {
    		VWMLContextBuilder.ContextBunch bunch = (VWMLContextBuilder.ContextBunch)entityWalker.getEntityMarkedAsIAS();
    		Object objLinkedId = id;
    		entityWalker.resetFutureEntityAsIAS();
       		// creates 'IAS' association
    		try {
    			// asking for current/active context
    			if (codeGenerator != null) {
    				if (logger.isDebugEnabled()) {
   					logger.debug("Interpreting bunch '" + bunch + "'");
   				}
   				
 				VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildReducedContextList();
    				for(VWMLContextBuilder.ContextBunchElement cbe = bunch.first(); cbe != null; cbe = bunch.next()) {
    					String firstRelatedContext = contexts.first();
    					for(String c = contexts.first(); c != null; c = contexts.next()) {
    						codeGenerator.interpretObjects(cbe.getId(), objLinkedId, c, c);
    						if (logger.isDebugEnabled()) {
   							logger.debug("Interpreting object '" + cbe.getId() + "' -> '" + objLinkedId + "'; on context '" + c + "'");
   						}
    					}
    				}				
    			}
    		}
    		catch(Exception e) {
    			rethrowSHLExceptionAsRecognitionException(e);
    		}	
    		return (codeGenerator != null) ? codeGenerator.getLastLink() : null;
	}
	
	protected Object buildLinkingAssociation(Object linkedObj) throws RecognitionException {
  		Object rel = entityWalker.peek();
  		if (rel == null) {
  			rel = EntityWalker.Relation.build(ComplexEntityNameBuilder.generateRootId(getCurrentFrameName()), EntityWalker.REL.NONE, null);
  		}
    		if (rel != null) {
    			try {
    				// asking for current/active context
    				VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
    				Object linkingObjId = ((EntityWalker.Relation)rel).getObj();
    				if (codeGenerator != null) {
    					for(String c = contexts.first(); c != null; c = contexts.next()) {
    						codeGenerator.linkObjects(linkingObjId, linkedObj, c, c, null);
    					}
    				}
    			}
    			catch(Exception e) {
    				rethrowSHLExceptionAsRecognitionException(e);
    			}
    		}
    		return (codeGenerator != null) ? codeGenerator.getLastLink() : null;
	}
	
	protected EntityWalker.Relation buildRelation(Object eId) throws RecognitionException {
		Object lastLink = null;
	     	EntityWalker.REL relType = EntityWalker.REL.NONE;
	    	// in case if entity was marked as IAS we have to build IAS association
	    	if (entityWalker.getEntityMarkedAsIAS() != null) {
	    		relType = EntityWalker.REL.ASSOCIATION;
	    		lastLink = buildIASAssociation(eId);
	    	}
	    	else {
	    		relType = EntityWalker.REL.LINK;
	    		// ... otherwise linkage
	  	
	  		lastLink = buildLinkingAssociation(eId);
	  	}
 		return EntityWalker.Relation.build(eId, relType, lastLink);
	}	
		
	/*
	*  **************************************** SHL functionality **************************************************
	*/
	protected void startProcessSHLFrame() throws RecognitionException {
		VWMLContextBuilder.ContextBunch bunch = (VWMLContextBuilder.ContextBunch)vwmlContextBuilder.peek();
		setCurrentFrameName(bunch.bunchAsString());
		if (getRootFrameName() == null) {
			setRootFrameName(getCurrentFrameName());
		}		
		declareAbsoluteContextByIASRelation();
    		complexEntityStartAssembling();		
	}
	
	protected void finishProcessSHLFrame() throws RecognitionException {
	        complexEntityStopAssembling();
		handleProcessedAbsoluteContextbyIASRelation();
	}
}


/*
*  *************************************** SHL grammar **************************************************
*
*/

main
    : filedef
    ;

props
    : 'options' '{' optionsList '}'
    ;


optionsList
    : vwmlSourcePath? moduleName?
    ;

vwmlSourcePath
    : 'vwmlSrcPath' '=' STRING_LITERAL { 
    					if (modProps != null) {
    						modProps.setVwmlSrcPath(GeneralUtils.trimQuotes($STRING_LITERAL.text));
    					}
    				}
    ;	

moduleName
    : 'moduleName' '=' STRING_LITERAL { 
        				if (modProps != null) {
    						modProps.setVwmlModuleName(GeneralUtils.trimQuotes($STRING_LITERAL.text));
    					}
       					if (logger.isDebugEnabled()) {
       						logger.debug("Module '" + modProps.getVwmlModuleName() + "' started");
       					}
    				  }
    ;

/*
*  *************************************** VWML grammar **************************************************
*
*/
filedef
    @init {
       	if (logger.isDebugEnabled()) {
       		logger.debug("Starting new module");
       	}        
    	if (codeGenerator == null) {
           	codeGenerator = shlModelBuilder.getCodeGenerator();
		// instantiating module's properties which will be filled later
		modProps = (codeGenerator != null) ? (ShlModuleStartProps)codeGenerator.buildProps() : null;
           	try {
           		codeGenerator.startModule(getModuleProps());
           	}
           	catch(Exception e) {
			logger.error("Caught exception '" + e + "'");
			rethrowSHLExceptionAsRecognitionException(e);           	
           	}
           	moduleInProgress = true;
        }
       	if (logger.isDebugEnabled()) {
       		logger.debug("Code generator '" + codeGenerator + "'");
       	}    
    }
    : props? (include (include)*)? shl_body EOF {
                             	try {
                             		moduleInProgress = false;
                             		if (includeDepth == 0) {
                             			((ShlModuleStartProps)getModuleProps()).getFrameCodeGeneratorProps().setGenerateFromFrame(getRootFrameName());
           					codeGenerator.generate(getModuleProps());
           				}
           				codeGenerator.finishModule(getModuleProps());
                             	}
                             	catch(Exception e) {
		    			logger.error("Caught exception '" + e + "'");
		    			rethrowSHLExceptionAsRecognitionException(e);
                             	}
                  	     }
    ;	 

include
    // includes eiher SHL or VWML items
    : include_item {
    			if (logger.isInfoEnabled()) {
    				logger.info("including '" + $include_item.id + "'");
    			}
    			// processes include depending on item's extension
    			processInclude(GeneralUtils.trimQuotes($include_item.id)); 
                   }
    ;
    
include_item returns [String id]
    :  'include' STRING_LITERAL {id = $STRING_LITERAL.text;}
    ;

shl_body
    : (content (content*))?
    ;


content
    : frame
    | schema
    ;

frames
    : (frame (frame)*)? 
    ;

schemas
    : (schema (schema)*)?
    ;

schema
    : 'schema' ID '=' term_def {
    	         } body
    ;

frame
    : 'frame' bunch_of_entity_decls {
    			startProcessSHLFrame();
                 } body
                 {
                 	finishProcessSHLFrame();
                 }
    ;
    
body
   : '{' (expression (expression)*)? '}'
   ;

 
expression
    : (bunch_of_entity_decls IAS) => entity_def
    | check_term_def
    ;

entity_def
    : bunch_of_entity_decls IAS {
    			// back constructing of bunch
    			declareAbsoluteContextByIASRelation();
    		      } (term)* SEMICOLON
    		      {
    		      	// reconstruct everything including terms
    		    	handleProcessedAbsoluteContextbyIASRelation();
    		      }
    		      
    ;

check_term_def
    : source_lifetrerm? LIFETERM '=' lifeterm_def
    | term_def
    ;

source_lifetrerm
    : 'source' {
    			// source lifeterm detected
    			if (logger.isDebugEnabled()) {
    				logger.debug("source lifeterm indicator detected");
    			}
    			sourceLifeTermDetectedFlag = true;
    	       }
    ;

lifeterm_def
    :  term_def {
    			if (logger.isInfoEnabled()) {
    				logger.info("Lifeterm '" + lastProcessedEntity + "' found");
    			}
    			if (codeGenerator != null) {
  	       			try {
  	       				String[] contexts = vwmlContextBuilder.buildContext().asStrings();
					codeGenerator.markEntityAsLifeTermOnContexts(lastProcessedEntity, sourceLifeTermDetectedFlag, contexts);
					if (logger.isDebugEnabled()) {
						logger.debug("entity '" + lastProcessedEntity + "' marked as lifeterm; is source '" + sourceLifeTermDetectedFlag + "'");
					}
					sourceLifeTermDetectedFlag = false;
				}
				catch(Exception e) {
					rethrowSHLExceptionAsRecognitionException(e);
				}
	       		}    
    		}
    ;

term_def
    : entity {
    		// reconstruct entity
    		lastProcessedEntity = $entity.rel;
    		lastProcessedEntityAsTerm = false;
    		if (lastProcessedEntity != null && logger.isDebugEnabled()) {
    			logger.debug(">> '" + lastProcessedEntity.getObj() + "' <<");
    		}    		
    	     } (oplist)* 
  	     {
  	     	// reconsstruct term including operations
  	       if (lastProcessedEntityAsTerm && codeGenerator != null) {
  	       		try {
  	       			VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
				if (logger.isDebugEnabled()) {
					logger.debug("entity '" + lastProcessedEntity + "' checking term prop on contexts '" + contexts + "'");
				}  	       						
				codeGenerator.markEntityAsTerm(lastProcessedEntity, contexts.asStrings());
				if (logger.isDebugEnabled()) {
					logger.debug("entity '" + lastProcessedEntity + "' marked as term on contexts '" + contexts + "'");
				}
			}
			catch(Exception e) {
				rethrowSHLExceptionAsRecognitionException(e);
			}
	       }   	     	  	     
	     }
    ;

entity_decl
    : simple_entity_decl  {
    				if (!complexEntityNameBuilderDecl.isInProgress()) {
    					addBunchElementToLastProcessedContextBunch($simple_entity_decl.id);
    				}
    			  }
    | complex_entity_decl {
    				Object id = complexEntityDeclarationPhase3();
    				addBunchElementToLastProcessedContextBunch(id);
    			  }
    ;


bunch_of_entity_decls
    @after {
    		handleLastProcessedContextBunch();
    }
    : 	{    	
    		createLastProcessedContextBunch();
	} entity_decl (COMMA entity_decl)*
    ;	

    
simple_entity_decl returns [String id]
    : ID { id = simpleEntityDeclaration($ID.getText()); }
    ;
    
complex_entity_decl
    : '(' {complexEntityDeclarationPhase1();} (entity_decl)* {complexEntityDeclarationPhase2();} ')'
    ;

term
    : expression
    ;  

entity returns [EntityWalker.Relation rel]
    : simple_entity         { 
    				rel = $simple_entity.rel;
    			    }

    | complex_entity        { 
    				rel = $complex_entity.rel;
    			    }
    | '.'                   {
                            	processComplexContext(lastProcessedEntity);
                            }
    ;


simple_entity returns [EntityWalker.Relation rel]
    : ID {
    		rel = simpleEntityAssembling($ID.text);
         }
    ;

complex_entity returns [EntityWalker.Relation rel]
    @init {
    	complexEntityStartAssembling();
    }
    @after {
        rel = complexEntityStopAssembling();
    }
    : '(' (term)* ')'
    ;
    
    
ID
    : LETTER (LETTER | '.')* // ('a'..'z'|'A'..'Z'|'0'..'9'|'.'|'_'|'*'|'-')+
    ;

STRING_LITERAL
    :  '"' ( ~('"') )* '"'
    ;    

oplist
    // associates operation with entity
    : opclist       {
    			if (lastProcessedEntity != null && codeGenerator != null) { 
    				lastProcessedEntityAsTerm = true;
    				lastProcessedEntity.setParentLink(entityWalker.peek());
    				VWMLContextBuilder.Contexts contexts = vwmlContextBuilder.buildContext();
    				codeGenerator.associateOperation(lastProcessedEntity, $opclist.text, contexts.first());
    				lastProcessedEntity.setParentLink(null);
    			} 
    		    }
    ;

opclist
    : OPJOIN
    | OPINTERSECT
    | OPSUBSTRUCT
    | OPFIRST
    | OPLAST
    | OPBEGIN
    | OPREST
    | OPCARTESIAN
    | OPIN
    | OPINCL
    | OPEQ
    | OPIDENT
    | OPSQU
    | OPINTERPRET
    | OPCREATEEXPR
    | OPEXECUTE
    | OPRANDOM
    | OPACTIVATECTX
    | OPACTIVATEONFRINGE
    | OPRELAX
    | OPSTARTCONFLICTGROUP
    | OPENDCONFLICTGROUP
    | OPBREAKPOINT
    | OPAPPLYTOCONTEXT
    | OPCLONE
    | OPPROJECTION
    | OPFOREACH
    | OPDYNCONTEXT
    | OPSIZE
    | OPINTERRUPT
    ;

termLanguages
    : JAVA
    | C
    | CPP
    | OBJECTIVEC
    ;

string
    : STRING_LITERAL
    ;
    
COMMA
    : ','
    ;
  
DQUOTE
    : '"'
    ;
    
SEMICOLON
    : ';'
    ;	

WS
    : (' '|'\t'|'\n'|'\r') {$channel=HIDDEN;}
    ;


COMMENT
    : '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;
    
LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;


fragment
LETTER
	: 'A'..'Z'
	| 'a'..'z'
	| '0'..'9' 
	| '_'
	| '-'
	| '!'
	| '?'
	| '$'
	;
