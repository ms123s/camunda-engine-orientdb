package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class GroupEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(GroupEntityHandler.class.getName());

	public GroupEntityHandler(ODatabaseSession g) {
		super( g, GroupEntity.class);
	}
}
