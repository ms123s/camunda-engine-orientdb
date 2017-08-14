package org.simpl4;

import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class Server {

  public static void main(String[] args) {


		OrientGraphFactory f = new OrientDB().getFactory( "camunda1", "root", "simpl4");
    ProcessEngine processEngine = new OrientdbProcessEngineConfiguration(f) .buildProcessEngine();
System.err.println("orientdb.processEngine:"+processEngine);

    try {

      // your code goes here...

    }
    finally {
      processEngine.close();
System.err.println("processEngine.close:");
    }

  }

}
