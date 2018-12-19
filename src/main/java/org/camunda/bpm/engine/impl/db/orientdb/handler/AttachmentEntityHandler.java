package org.camunda.bpm.engine.impl.db.orientdb.handler;

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import com.orientechnologies.orient.core.db.ODatabaseSession;

/**
 * @author Manfred Sattler
 */
public class AttachmentEntityHandler extends BaseEntityHandler{
	private final static Logger log = Logger.getLogger(AttachmentEntityHandler.class.getName());

	public AttachmentEntityHandler(ODatabaseSession g) {
		super( g, AttachmentEntity.class);
	}
}
