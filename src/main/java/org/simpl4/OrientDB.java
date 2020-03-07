package org.simpl4;
import java.util.logging.Logger;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDBConfig;


public class OrientDB {
	private static Logger LOG = Logger.getLogger(OrientDB.class.getName());

	private static com.orientechnologies.orient.core.db.OrientDB orientDB;
	private static String rootPassword = "simpl4";

	public synchronized ODatabasePool getDatabasePool(String db,String username, String pw ) {
		if( orientDB == null){
			orientDB = new com.orientechnologies.orient.core.db.OrientDB("remote:192.168.2.97", "root", rootPassword, OrientDBConfig.defaultConfig());
		}
		if (!orientDB.exists(db)) {
			orientDB.create(db,ODatabaseType.PLOCAL);
		}
		System.err.println("getDatabasePool("+username+","+pw);
	  ODatabasePool pool = new ODatabasePool(orientDB, db, username, pw);
		return pool;
	}
}

