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

		def cwd = System.getProperty("user.dir");
		def outDir = new File( cwd, "src/main/java/org/camunda/bpm/engine/impl/db/orientdb/handler");
		def outHandlerMap = new File( cwd, "etc/HandlerMap.txt");
		outHandlerMap.write("");
		def templateContent = this.getClass().getResource( '/EntityTemplate.java' ).text;

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.addClassLoader( TaskEntity.class.getClassLoader());
		cb.setScanners(new SubTypesScanner());
		cb.setUrls(ClasspathHelper.forPackage("org.camunda"));
		Reflections r = new Reflections( cb );
		Set<Class<? extends DbEntity>> modules = r.getSubTypesOf(DbEntity.class);

		for( Class de : modules){
			System.out.println(de.getName());
			def binding =[
				entityName: de.getSimpleName(),
				entityNameWithPackage: de.getName()
			];
			def template = new groovy.text.StreamingTemplateEngine().createTemplate(templateContent);
	   	def classContent = template.make(binding).toString();
			def outFile = new File( outDir, de.getSimpleName()+"Handler.java");
			outFile.write( classContent);
			outHandlerMap.append("entityHandlerMap.put(${binding.entityName}.class, new ${binding.entityName}Handler(orientGraph));\n");
			//new PrintHier( de).printHierarchy();
		}
	}

}

