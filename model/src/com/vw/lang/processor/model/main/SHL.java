package com.vw.lang.processor.model.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.vw.lang.processor.model.builder.SHLModelBuilder;

/**
 * SHL's processor main class
 * @author ogibayev
 *
 */
public final class SHL {
	
	
	public static abstract class Operation {
		
		public enum ARGS {
			SHLFILE
		}
		
		public void process(VWMLArgs args) throws Exception {
			run(args);
			finalProcedure(args);
		}
		
		public void run(VWMLArgs args) throws Exception {
			String filePath = args.getArguments().get(Operation.ARGS.SHLFILE.ordinal());
			if (args.getOutVwmlDir() != null) {
				SHLModelBuilder.instance().getProjectProps().addProperty(SHLModelBuilder.COMPILATION_OPTIONS.OUTDIR.toValue(), args.getOutVwmlDir());
			}
			if (args.getOutVwml() != null) {
				SHLModelBuilder.instance().getProjectProps().addProperty(SHLModelBuilder.COMPILATION_OPTIONS.OUTFILE.toValue(), args.getOutVwml());
			}
			// the module's props are set during compilation phase (see grammar file, term 'filedef')
			SHLModelBuilder.instance().compile(filePath);
		}
		
		/**
		 * Called upon operation's final step
		 */
		protected void finalProcedure(VWMLArgs args) throws Exception {
			SHLModelBuilder.instance().finalProcedure(SHLModelBuilder.instance().getProjectProps());
		}
	}
	
	/**
	 * Compiles VWML sources into given language only
	 * @author ogibayev
	 *
	 */
	public static class Sources extends Operation {

		@Override
		public void run(VWMLArgs args) throws Exception {
			SHLModelBuilder.instance().setBuildSteps(SHLModelBuilder.BUILD_STEPS.SOURCE);
			super.run(args);
		}
	}

	/**
	 * Defines interpretator's properties, it isn't in usage now
	 * @author ogibayev
	 *
	 */
	public static class VWMLInterprterArgs {
		@Option(name = "-src", usage="absolute path, where interpreter's sources should be located; usually coincides with module's sources")
		private String srcPath;
		@Option(name = "-package", usage="interpreter's java package")
		private String pkg;
		
		public String getSrcPath() {
			return srcPath;
		}
		public void setSrcPath(String srcPath) {
			this.srcPath = srcPath;
		}
		public String getPkg() {
			return pkg;
		}
		public void setPkg(String pkg) {
			this.pkg = pkg;
		}
		@Override
		public String toString() {
			return "VWMLInterprterArgs [srcPath=" + srcPath + ", pkg=" + pkg
					+ "]";
		}
	}
	
	/**
	 * Command line arguments
	 * @author ogibayev
	 *
	 */
	public static class VWMLArgs {
		@Option(name="-m", usage="compiling mode {source};\r\nsource - generates VWML from SHL\r\n")
		private String mode;
		@Option(name="-d", usage="output directory for generated VWML code\r\n")
		private String outVwmlDir;
		@Option(name="-o", usage="output file\r\n")
		private String outVwml;
		
		 // receives other command line parameters than options
	    @Argument
	    private List<String> arguments = new ArrayList<String>();

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public String getOutVwmlDir() {
			return outVwmlDir;
		}

		public void setOutVwmlDir(String outVwmlDir) {
			this.outVwmlDir = outVwmlDir;
		}

		public String getOutVwml() {
			return outVwml;
		}

		public void setOutVwml(String outVwml) {
			this.outVwml = outVwml;
		}

		public List<String> getArguments() {
			return arguments;
		}

		public void setArguments(List<String> arguments) {
			this.arguments = arguments;
		}

		@Override
		public String toString() {
			return "VWMLArgs [mode=" + mode + ", outVwmlDir=" + outVwmlDir
					+ ", outVwml=" + outVwml + ", arguments=" + arguments + "]";
		}
	}
	
	@SuppressWarnings("serial")
	private static Map<String, Operation> s_opCodes = new HashMap<String, Operation>() {
		{put("source",  new Sources());}
	};
	
	private static Logger logger = Logger.getLogger(SHL.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VWMLArgs vwmlArgs = new VWMLArgs();
		CmdLineParser cmdParser = new CmdLineParser(vwmlArgs);
		cmdParser.setUsageWidth(80);
		try {
			cmdParser.parseArgument(args);
			if (logger.isInfoEnabled()) {
				logger.info("builder started; actual arguments are '" + vwmlArgs + "'");
			}
			Operation op = s_opCodes.get(vwmlArgs.getMode());
			if (op != null) {
				op.process(vwmlArgs);
			}
			else {
				logger.error("invalid mode '" + vwmlArgs.getMode() + "'; valid are 'source | test'");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
