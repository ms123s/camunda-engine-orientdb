package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class MessageEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(MessageEntityHandler.class.getName());

	public MessageEntityHandler(OrientGraph g) {
		super( g, MessageEntity.class);
	}
	@Override
	protected void setSuperClasses(OSchema schema, OClass oClass, boolean restricted) {
		List<OClass> superList = new ArrayList<OClass>();
		superList.add(schema.getClass("JobEntity"));
		oClass.setSuperClasses(superList);
	}

	@Override
	public void insertAdditional(OrientGraph orientGraph, Vertex v, Object entity, Class entityClass, Map<String, Vertex> entityCache) {
		v.setProperty( "type", "MessageEntity");
	}
	@Override
	protected OProperty getOrCreateProperty(OClass oClass, String propertyName, OType oType) {
		return null;
	}
}
