package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class ByteArrayEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(ByteArrayEntityHandler.class.getName());

	public ByteArrayEntityHandler(ODatabaseSession g) {
		super( g, ByteArrayEntity.class);
	}
}
