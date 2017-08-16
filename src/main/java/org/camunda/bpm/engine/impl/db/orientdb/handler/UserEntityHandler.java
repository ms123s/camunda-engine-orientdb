package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class UserEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(UserEntityHandler.class.getName());

	public UserEntityHandler(OrientGraph g) {
		super( g, UserEntity.class);
	}
}
