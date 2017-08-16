package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricExternalTaskLogEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricExternalTaskLogEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricExternalTaskLogEntityHandler.class.getName());

	public HistoricExternalTaskLogEntityHandler(OrientGraph g) {
		super( g, HistoricExternalTaskLogEntity.class);
	}
}
