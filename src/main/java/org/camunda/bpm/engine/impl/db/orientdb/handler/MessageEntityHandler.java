package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OVertex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;

/**
 * @author Manfred Sattler
 */
public class MessageEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(MessageEntityHandler.class.getName());

	public MessageEntityHandler(ODatabaseSession g) {
		super( g, MessageEntity.class);
	}
	@Override
	protected void setSuperClasses(OSchema schema, OClass oClass, boolean restricted) {
		List<OClass> superList = new ArrayList<OClass>();
		superList.add(schema.getClass("JobEntity"));
		oClass.setSuperClasses(superList);
	}

	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
		v.setProperty( "type", "MessageEntity");
	}
	@Override
	protected OProperty getOrCreateProperty(OClass oClass, String propertyName, OType oType) {
		return null;
	}
}
