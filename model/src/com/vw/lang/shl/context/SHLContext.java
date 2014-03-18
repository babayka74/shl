package com.vw.lang.shl.context;

import java.util.HashMap;
import java.util.Map;

import com.vw.lang.generator.strategies.StrategyFactory;
import com.vw.lang.processor.context.builder.VWMLContextBuilder;
import com.vw.lang.shl.entity.SHLEntity;
import com.vw.lang.shl.repositories.SHLContextsRepository;
import com.vw.lang.sink.java.VWMLObject;

/**
 * SHL context; In SHL context, mainly, is represented by frame, but in many cases it can be considered 
 * as projection of regular VWML context which is defined by 'IAS' keyword
 * @author Oleg
 *
 */
public class SHLContext extends VWMLObject {

	private String contextName;
	private VWMLContextBuilder.ContextBunch bunch;
	private SHLEntity iasRelation;
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

	public SHLEntity getIasRelation() {
		return iasRelation;
	}

	public void setIasRelation(SHLEntity iasRelation) {
		this.iasRelation = iasRelation;
	}

	public void associateEntity(SHLEntity e) {
		entities.put(e.getId(), e);
		e.setContext(this);
	}
	
	public SHLEntity getEntityFromContext(Object id) {
		return entities.get(id);
	}
	
	public void associateWithBunch(VWMLContextBuilder.ContextBunch bunch) {
		this.bunch = bunch;
	}
	
	public void unAssociateBunch(VWMLContextBuilder.ContextBunch bunch) {
		this.bunch = null;
	}
	
	public VWMLContextBuilder.ContextBunch getAssociatedBunch() {
		return this.bunch;
	}
	
	public void unAssociateEntity(SHLEntity e) {
		entities.remove(e.getId());
		e.setContext(null);
	}

	public void unAssociateEntity(Object id) {
		SHLEntity entity = findEntity(id);
		if (entity != null) {
			entity.setContext(null);
			entities.remove(id);
		}
	}
	
	public SHLEntity findEntityEx(Object id, Object origId) {
		SHLEntity e = findEntity(id);
		if (e != null) {
			e.setId(origId);
		}
		return e;
	}
	
	public SHLEntity findEntity(Object id) {
		SHLEntity e = null;
		if (((String)id).contains(".")) {
			String contextId = ((String)id).substring(0, ((String)id).lastIndexOf("."));
			SHLContext ctx = SHLContextsRepository.instance().get(contextId);
			if (ctx != null) {
				e = ctx.findEntityEx(((String)id).substring(((String)id).lastIndexOf(".") + 1), id);
			}
			else {
				ctx = StrategyFactory.getStratgeyForUndefinedEntities().undefinedContextEncountered(contextId);
				if (ctx != null) {
					e = ctx.findEntityEx(((String)id).substring(((String)id).lastIndexOf(".") + 1), id);
				}
			}
		}
		else {
			SHLContext ctx = this;
			e = entities.get(id);
			while (e == null) {
				ctx = (SHLContext)ctx.getLink().getParent();
				if (ctx == null) {
					break;
				}
				e = ctx.getEntityFromContext(id);
			}
		}
		if (e == null) {
			e = StrategyFactory.getStratgeyForUndefinedEntities().undefinedEntityEncountered(id, this);
		}
		return e;
	}
	
	public void addChildren(SHLContext child) {
		getLink().link(child);
	}

	public void removeChildren(SHLContext child) {
		getLink().unlinkFrom(child);
	}
}
