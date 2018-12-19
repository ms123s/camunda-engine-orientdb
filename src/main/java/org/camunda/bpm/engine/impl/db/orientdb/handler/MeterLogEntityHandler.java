package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.MeterLogEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class MeterLogEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(MeterLogEntityHandler.class.getName());

	public MeterLogEntityHandler(ODatabaseSession g) {
		super( g, MeterLogEntity.class);
	}
}
