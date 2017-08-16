package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionStatisticsEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class ProcessDefinitionStatisticsEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ProcessDefinitionStatisticsEntityHandler.class.getName());

	public ProcessDefinitionStatisticsEntityHandler(OrientGraph g) {
		super( g, ProcessDefinitionStatisticsEntity.class);
	}
}
