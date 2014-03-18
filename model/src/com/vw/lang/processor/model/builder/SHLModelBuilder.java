package com.vw.lang.processor.model.builder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;

import com.vw.common.Debuggable;
import com.vw.lang.generator.VWMLCodeGenerator;
import com.vw.lang.grammar.SchemaLanguageLexer;
import com.vw.lang.grammar.SchemaLanguageParser;
import com.vw.lang.sink.ICodeGenerator;
import com.vw.lang.sink.ICodeGenerator.StartModuleProps;


/**
 * SHL's model builder
 * @author ogibayev
 *
 */
public class SHLModelBuilder extends Debuggable {

	/**
	 * Defines compilation options
	 * @author Oleg
	 *
	 */
	public static enum COMPILATION_OPTIONS {
		OUTDIR("outDir"),
		OUTFILE("outFile"),
		UNKNOWN("");
		
		private String value;
		
		private COMPILATION_OPTIONS(String value) {
			this.value = value;
		}
		
		 public static COMPILATION_OPTIONS fromValue(String value) {  
			if (value != null) {  
				 for (COMPILATION_OPTIONS tm : values()) {  
					 if (tm.value.equals(value)) {  
						 return tm;  
					 }  
				 }  
			}  
			return getDefault();  
		 }
		 
		 public String toValue() {
			 return value;
		 }
		 
		 public static COMPILATION_OPTIONS getDefault() {
			 return COMPILATION_OPTIONS.UNKNOWN;
		 }
	}
	
	/**
	 * Defines possible SHL building steps
	 * @author ogibayev
	 *
	 */
	public static enum BUILD_STEPS {
		SOURCE
	}
	
	// Default build steps
	private String projectPath = null;
	private StartModuleProps projectProps = new StartModuleProps();
	private BUILD_STEPS buildSteps = null;
	// builder is implemented as singleton
	private static volatile SHLModelBuilder s_builder = null;
	private static Map<String, SHLModuleInfo> s_modulesInfo = new HashMap<String, SHLModuleInfo>();

	private final Logger logger = Logger.getLogger(SHLModelBuilder.class);
	
	private SHLModelBuilder() {
		
	}
	
	/**
	 * Creates and initializes builder
	 * @return
	 * @throws Exception
	 */
	public static SHLModelBuilder instance() {
		if (s_builder != null) {
			return s_builder;
		}
		synchronized(SHLModelBuilder.class) {
			if (s_builder != null) {
				return s_builder;
			}
			SHLModelBuilder builder = new SHLModelBuilder();
			try {
				builder.init();
				s_builder = builder;
			} catch (Exception e) {
				builder.trace("exception caught '" + e + "'");
			}
		}
		return s_builder;
	}

	/**
	 * Returns reference to module's information by its name
	 * @param name
	 * @return
	 */
	public static SHLModuleInfo getModuleInfo(String name) {
		return s_modulesInfo.get(name);
	}
	
	/**
	 * Returns set of VWML processed modules
	 * @return
	 */
	public static Set<String> getProcessedModules() {
		return s_modulesInfo.keySet();
	}
	
	/**
	 * Associates module's name and module info structure
	 * @param name
	 * @param mi
	 */
	public static void addModuleInfo(String name, SHLModuleInfo mi) {
		s_modulesInfo.put(name, mi);
	}
	
	public BUILD_STEPS getBuildSteps() {
		return buildSteps;
	}

	public void setBuildSteps(BUILD_STEPS buildSteps) {
		this.buildSteps = buildSteps;
	}
	
	public StartModuleProps getProjectProps() {
		return projectProps;
	}

	public void setProjectProps(StartModuleProps modProps) {
		this.projectProps = modProps;
	}

	/**
	 * Returns code generator associated with given sink
	 * @param sink
	 * @return
	 */
	public ICodeGenerator getCodeGenerator() {
		return new VWMLCodeGenerator();
	}

	/**
	 * Normalizes properties by filling non-set values by project properties
	 * @param props
	 * @return
	 */
	public StartModuleProps normalizeProps(StartModuleProps props) {
		return props;
	}

	/**
	 * Runs builder's initialization process
	 * @throws Exception
	 */
	public void init() throws Exception {
	}
	

	@Override
	public void debugEnter(Object to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void debugExit(Object from) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String trace) {
		if (isTraceable()) {
			if (isDebug()) {
				System.out.println(trace);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug(trace);
				}
			}
		}
	}
	
	/**
	 * Compiles SHL to VWML
	 * @param shlFilePath
	 * @throws Exception
	 */
	public void compile(String shlFilePath) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("compiling '" +  shlFilePath + "'");
		}
		if (projectPath == null) {
			projectPath = new File(shlFilePath).getParent();
		}
		else {
			if (!new File(shlFilePath).isAbsolute()) {
				shlFilePath = projectPath + "/" + shlFilePath;
			}
		}
        SchemaLanguageLexer lex = new SchemaLanguageLexer(new ANTLRFileStream(shlFilePath, "UTF8"));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        SchemaLanguageParser g = new SchemaLanguageParser(tokens);
        try {
            g.filedef();
    		if (logger.isInfoEnabled()) {
    			logger.info("compiled '" +  shlFilePath + "'... OK");
    		}
        }
        catch (SchemaLanguageParser.SHLCodeGeneratorRecognitionException e) {
        	logger.error("couldn't compile '" + shlFilePath + "'; error is '" + e.getCause().getMessage() + "'");
            throw e;
        }
        catch (RecognitionException e) {
        	logger.error("couldn't compile '" + shlFilePath + "'; error is '" + e.getMessage() + "'; position '" + e.line + ":" + e.charPositionInLine + "'; token '" + ((e.token != null) ? e.token.getText() : "undefined" + "'"));
            throw e;
        } 
	}
	
	/**
	 * Final step in source generation phase
	 */
	public void finalProcedure(StartModuleProps props) throws Exception {
	}
}
