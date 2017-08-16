package org.simpl4;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.logging.*;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import org.reflections.Reflections;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.reflections.scanners.SubTypesScanner;
import org.camunda.bpm.engine.impl.db.DbEntity;





public class Scanner {

	private static void initLog(){
		Logger LOG = LogManager.getLogManager().getLogger("");
		LOG.setUseParentHandlers(false);

		Handler[] handlers = LOG.getHandlers();
		for(Handler handler : handlers) {
			LOG.removeHandler(handler);
		}

		LogFormatter formatter = new LogFormatter();
		ConsoleHandler newHandler = new ConsoleHandler();
		newHandler.setFormatter(formatter);
		LOG.addHandler(newHandler);
	}

	public static void main(String[] args) {
		initLog();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.addClassLoader( TaskEntity.class.getClassLoader());
		cb.setScanners(new SubTypesScanner());
		cb.setUrls(ClasspathHelper.forPackage("org.camunda"));
		Reflections r = new Reflections( cb );
		Set<Class<? extends DbEntity>> modules = r.getSubTypesOf(DbEntity.class);
		for( Class de : modules){
			System.out.println("\n");
			new PrintHier( de).printHierarchy();
		}
	}

}

