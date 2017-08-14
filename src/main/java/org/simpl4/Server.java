package org.simpl4;

import org.camunda.bpm.engine.impl.cfg.orientdb.OrientdbProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngine;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Server {

	public static void main(String[] args) {
		Logger LOG = LogManager.getLogManager().getLogger("");
		LOG.setUseParentHandlers(false);

		LogFormatter formatter = new LogFormatter();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);

		Handler[] handlers = LOG.getHandlers();
		for(Handler h : handlers) {
			LOG.removeHandler(h);
		}
		LOG.addHandler(handler);


		OrientGraphFactory f = new OrientDB().getFactory("camunda1", "root", "simpl4");
		f.setStandardElementConstraints(false);

		ProcessEngine processEngine = new OrientdbProcessEngineConfiguration(f).buildProcessEngine();
		LOG.info("orientdb.processEngine:" + processEngine);

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
			LOG.info("processEngine.close");
		}

	}

}

