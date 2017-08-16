package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class IdentityInfoEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(IdentityInfoEntityHandler.class.getName());

	public IdentityInfoEntityHandler(OrientGraph g) {
		super( g, IdentityInfoEntity.class);
	}
}
