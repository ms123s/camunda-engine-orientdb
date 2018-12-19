package org.simpl4;
import java.util.logging.Logger;
import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.security.OServerSecurity;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;

import org.camunda.bpm.engine.ProcessEngineException;

public class OrientDB {
	private static Logger LOG = Logger.getLogger(OrientDB.class.getName());

	private OServer server;
	private OServerAdmin serverAdmin;
	private static String rootPassword = "simpl4";

	private OServerAdmin getServerAdmin() {
		if( serverAdmin != null) return serverAdmin;
		try{
			serverAdmin = new OServerAdmin("remote:192.168.2.5").connect("root", this.rootPassword);
			LOG.info("OrientDBService.serverAdmin:" + serverAdmin);
		}catch(Exception e){
     throw new ProcessEngineException("initOriendb failed: " + e.getMessage(), e);
		}
		return serverAdmin;
	}

	public synchronized OrientGraphFactory getFactory(String name, String user, String pw) {
		LOG.info("getFactory1("+name+"):"+user);
		
		if (!dbExists(name)) {
			dbCreate(name);
		}
		
		OrientGraphFactory f = new OrientGraphFactory("remote:192.168.2.5/" + name, user, pw, true);
		f.setupPool(1, 20);
		f.setAutoStartTx(false);
		LOG.info("getFactory2("+name+")"+f);
		return f;
	}
	private void dbCreate(String name) {
		try {
			getServerAdmin().createDatabase(name, "graph", "plocal");
			LOG.info("dbCreate("+name+")");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.throwing("OrientdbProcessEngineConfiguration", "dbCreate", e);
		}
	}
	private boolean dbExists(String name) {
		try {
			boolean b = getServerAdmin().existsDatabase(name, "plocal");
			LOG.info("dbExists("+name+"):"+b);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.throwing("OrientdbProcessEngineConfiguration", "dbExists", e);
			return false;
		}
	}
}
