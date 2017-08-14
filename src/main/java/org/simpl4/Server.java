package org.simpl4;

import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;

public class Server {

	public static void main(String[] args) {

		OrientGraphFactory f = new OrientDB().getFactory("camunda1", "root", "simpl4");
f.setStandardElementConstraints(false);

		ProcessEngine processEngine = new OrientdbProcessEngineConfiguration(f).buildProcessEngine();
		System.err.println("orientdb.processEngine:" + processEngine);

		try {

			RepositoryService repositoryService = processEngine.getRepositoryService();
			RuntimeService runtimeService = processEngine.getRuntimeService();
			String deploymentId = repositoryService.createDeployment().
				addModelInstance("process1.bpmn", Bpmn.createExecutableProcess("testProcess").
				startEvent().
				endEvent().done()).
				deploy().getId();

			//runtimeService.startProcessInstanceByKey("testProcess", createVariables().putValue("foo", stringValue("bar")));

		} finally {
			processEngine.close();
			System.err.println("processEngine.close");
		}

	}

}

