package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;

/**
 * @author Manfred Sattler
 */
public class TimerEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(TimerEntityHandler.class.getName());

	public TimerEntityHandler(ODatabaseSession g) {
		super( g, TimerEntity.class);
	}

	@Override
	protected void setSuperClasses(OSchema schema, OClass oClass, boolean restricted) {
		List<OClass> superList = new ArrayList<OClass>();
		superList.add(schema.getClass("JobEntity"));
		oClass.setSuperClasses(superList);
	}

	@Override
	public void insertAdditional(OVertex v, Object entity, Map<Object, List<OVertex>> entityCache) {
		v.setProperty( "type", "TimerEntity");
	}
	@Override
	protected OProperty getOrCreateProperty(OClass oClass, String propertyName, OType oType) {
		return null;
	}
}
