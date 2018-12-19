package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionStatisticsEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionStatisticsEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ProcessDefinitionStatisticsEntityHandler.class.getName());

	public ProcessDefinitionStatisticsEntityHandler(ODatabaseSession g) {
		super( g, ProcessDefinitionStatisticsEntity.class);
	}
}
