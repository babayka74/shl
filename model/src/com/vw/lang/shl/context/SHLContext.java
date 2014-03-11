package com.vw.lang.shl.context;

import java.util.HashMap;
import java.util.Map;

import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.sink.java.VWMLObject;

/**
 * SHL context; In SHL context, mainly, is represented by frame, but in many cases it can be considered 
 * as projection of regular VWML context which is defined by 'IAS' keyword
 * @author Oleg
 *
 */
public class SHLContext extends VWMLObject {

	private String contextName;
	private Map<Object, SHLEntity> entities = new HashMap<Object, SHLEntity>();
	
	public SHLContext(Object hashId) {
		super(hashId);
	}
	
	public SHLContext(Object hashId, Object id, String readableId) {
		super(hashId, id, readableId);
	}
	
	public static SHLContext instance(Object hashId) {
		return new SHLContext(hashId);
	}
	
	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public void associateEntity(SHLEntity e) {
		entities.put(e.getId(), e);
	}
	
	public void unAssociateEntity(SHLEntity e) {
		entities.remove(e.getId());
	}

	public void unAssociateEntity(Object id) {
		entities.remove(id);
	}
	
	public SHLEntity findEntity(Object id) {
		return entities.get(id);
	}
	
	public void addChildren(SHLContext child) {
		getLink().link(child);
	}

	public void removeChildren(SHLContext child) {
		getLink().unlinkFrom(child);
	}
}
