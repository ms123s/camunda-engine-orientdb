package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class HistoricDetailVariableInstanceUpdateEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(HistoricDetailVariableInstanceUpdateEntityHandler.class.getName());

	public HistoricDetailVariableInstanceUpdateEntityHandler(OrientGraph g) {
		super( g, HistoricDetailVariableInstanceUpdateEntity.class);
	}
}
