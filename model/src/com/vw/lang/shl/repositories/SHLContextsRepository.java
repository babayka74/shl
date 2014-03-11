package com.vw.lang.shl.repositories;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vw.lang.shl.context.SHLContext;
import com.vw.lang.sink.java.VWMLJavaExportUtils;
import com.vw.lang.sink.java.VWMLObject;
import com.vw.lang.sink.utils.GeneralUtils;

/**
 * SHL contexts' repository
 * @author Oleg
 *
 */
public class SHLContextsRepository {
	private static String s_default_context = "__vwml_root_context__";
	// internal logger
	private static Logger logger = Logger.getLogger(SHLContextsRepository.class);
	
	private SHLContextsRepository() {
		createContextIfNotExists(s_default_context);
	}
	
	private Map<Object, SHLContext> contextsMap = new HashMap<Object, SHLContext>();
	
	private static final SHLContextsRepository s_contextsRepository = new SHLContextsRepository();
	
	public static SHLContextsRepository instance() {
		return s_contextsRepository;
	}
	
	/**
	 * Returns id of default context
	 * @return
	 */
	public static String getDefaultContextId() {
		return s_default_context;
	}

	/**
	 * Creates and registers in repository in case if context doesn't exist, otherwise reference to existed context is returned
	 * @param contextId
	 * @return
	 */
	public SHLContext createContextIfNotExists(Object contextId) {
		contextId = normalizeContext((String)contextId);
		String[] contextPath = VWMLJavaExportUtils.parseContext((String)contextId);
		return createFromContextPath(contextPath);
	}

	/**
	 * Allows to create context (if not exists) by context path
	 * @param contextPath
	 * @return
	 */
	public SHLContext createFromContextPath(String[] contextPath) {
		String rootContext = null;
		int startCtxIndex = 1;
		if (contextPath == null || contextPath.length == 0 || contextPath[0].length() == 0) {
			rootContext = s_default_context;
		}
		else {
			rootContext = contextPath[0];
			if (!rootContext.equals(getDefaultContextId())) {
				rootContext = getDefaultContextId();
				startCtxIndex = 0;
			}
		}
		SHLContext root = contextsMap.get(rootContext);
		if (root == null) {
			root = SHLContext.instance(rootContext);
			root.setContextName(rootContext);
			root.setId(rootContext);
			contextsMap.put(rootContext, root);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Creating context '" + GeneralUtils.toString(contextPath, ".") + "'");
		}
		return create(root, contextPath, startCtxIndex);
	}
	
	/**
	 * Returns context identified by id
	 * @param contextId
	 * @return
	 */
	public SHLContext get(Object contextId) {
		contextId = normalizeContext((String)contextId);
		String[] contextPath = VWMLJavaExportUtils.parseContext((String)contextId);
		String rootContext = null;
		if (contextPath == null || contextPath.length == 0 || contextPath[0].length() == 0) {
			rootContext = s_default_context;
		}
		else {
			rootContext = contextPath[0];
		}
		SHLContext root = contextsMap.get(rootContext);
		if (root == null) {
			return null;
		}		
		return find(root, contextPath, null, 1, -1);
	}

	/**
	 * Returns main root context
	 * @return
	 */
	public SHLContext getRootContext() {
		String rootContext = s_default_context;
		SHLContext root = contextsMap.get(rootContext);
		return root;
	}
	
	protected String normalizeContext(String context) {
		if (context == null || context.length() == 0 || context.equals(s_default_context)) {
			return s_default_context;
		}
		if (context.startsWith(s_default_context)) {
			return context;
		}
		return s_default_context + "." + context; 
	}
	
	protected SHLContext create(SHLContext parent, String[] contextPath, int pos) {
		String actualContext = "";
		for(int i = pos; i < contextPath.length; i++) {
			VWMLObject next = null;
			for(VWMLObject o : parent.getLink().getLinkedObjects()) {
				if (o.getId().equals(contextPath[i])) {
					next = o;
					break;
				}
			}
			if (actualContext.length() > 0) {
				actualContext += ".";
			}
			actualContext += contextPath[i];
			if (next == null) {
				next = SHLContext.instance(actualContext);
				((SHLContext)next).setContextName(contextPath[i]);
				((SHLContext)next).setId(contextPath[i]);
				parent.getLink().link(next);
			}
			parent = (SHLContext)next;
		}
		return parent;
	}
		
	protected SHLContext find(SHLContext parent, String[] contextPath, String contextElement, int pos, int toPos) {
		VWMLObject next = null;
		int tillPos = (toPos == -1) ? contextPath.length : toPos;
		for(int i = pos; i < tillPos; i++) {
			next = null;
			for(VWMLObject o : parent.getLink().getLinkedObjects()) {
				if (((SHLContext)o).getContextName().equals(contextPath[i])) {
					next = o;
					break;
				}
			}
			if (next == null) {
				parent = null;
				break;
			}
			parent = (SHLContext)next;
		}
		if (contextElement != null && parent != null) {
			next = null;
			for(VWMLObject o : parent.getLink().getLinkedObjects()) {
				if (((SHLContext)o).getContextName().equals(contextElement)) {
					next = o;
					break;
				}
			}
			parent = (SHLContext)next;
		}
		return parent;
	}
}
