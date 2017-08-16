package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseActivityInstanceEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricCaseActivityInstanceEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricCaseActivityInstanceEntityHandler.class.getName());

	public HistoricCaseActivityInstanceEntityHandler(OrientGraph g) {
		super( g, HistoricCaseActivityInstanceEntity.class);
	}
}
