package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Manfred Sattler
 */
public class GroupEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(GroupEntityHandler.class.getName());

	public GroupEntityHandler(OrientGraph g) {
		super( g, GroupEntity.class);
	}
}
