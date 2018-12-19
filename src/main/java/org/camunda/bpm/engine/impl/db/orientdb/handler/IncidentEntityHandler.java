package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class IncidentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(IncidentEntityHandler.class.getName());

	public IncidentEntityHandler(ODatabaseSession g) {
		super( g, IncidentEntity.class);
	}
}
