package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class ExternalTaskEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ExternalTaskEntityHandler.class.getName());

	public ExternalTaskEntityHandler(OrientGraph g) {
		super( g, ExternalTaskEntity.class);
	}
}
