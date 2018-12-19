/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cfg.orientdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.standalone.StandaloneTransactionContextFactory;
import org.camunda.bpm.engine.impl.db.orientdb.OrientdbPersistenceProviderFactory;
import org.camunda.bpm.engine.impl.db.orientdb.OrientdbSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.ProcessEngineException;

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
import com.orientechnologies.orient.core.db.ODatabasePool;
import java.lang.reflect.Method;

/**
 * @author Manfred Sattler
 *
 */
public class OrientdbProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

	private static Logger LOG = Logger.getLogger(OrientdbProcessEngineConfiguration.class.getName());

	protected ODatabasePool databasePool;

	public static List<String> members = new ArrayList<String>();

	public static String manager = null;

	public OrientdbProcessEngineConfiguration() {
		super();
		createDatabaseFactory("camunda2", "root", "simpl4");

		setHistory(HISTORY_FULL);
		setCmmnEnabled(false);
		setDmnEnabled(false);
		setAuthorizationEnabled(false);
		setMetricsEnabled(false);
		setJobExecutorActivate(false);
		setDbMetricsReporterActivate(false);
		setDeploymentLockUsed(false);
		setTenantCheckEnabled(false);
		setEnableExpressionsInAdhocQueries(true);
		setExecutionTreePrefetchEnabled(false);
		CommandContextFactory ccf = createDefaultCommandContextFactory();
		ccf.setProcessEngineConfiguration(this);
		setCommandContextFactory(ccf);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createDatabaseFactory(String database, String user, String pw) {
		try {
			Class clazz = Class.forName("org.simpl4.OrientDB");
			Object object = clazz.newInstance();
			Method method = clazz.getMethod("getDatabasePool", new Class[] { String.class, String.class, String.class });
			databasePool = (ODatabasePool) method.invoke(object, new Object[] { database, user, pw });
		} catch (Exception e) {
			throw new RuntimeException("Error createDatabaseFactory", e);
		}
	}

	public OrientdbProcessEngineConfiguration(ODatabasePool f) {
		super();

		setDatabaseFactory( f );
		setHistory(HISTORY_FULL);
		setCmmnEnabled(false);
		setDmnEnabled(false);
		setAuthorizationEnabled(false);
		setMetricsEnabled(false);
		setJobExecutorActivate(false);
		setDbMetricsReporterActivate(false);
		setDeploymentLockUsed(false);
		setTenantCheckEnabled(false);
		setEnableExpressionsInAdhocQueries(true);
		setExecutionTreePrefetchEnabled(false);
		CommandContextFactory ccf = createDefaultCommandContextFactory();
		ccf.setProcessEngineConfiguration(this);
		setCommandContextFactory(ccf);
	}

	private CommandContextFactory createDefaultCommandContextFactory() {
		return new CommandContextFactory();
	}

	private void setDatabaseFactory( ODatabasePool p){
		//f.setStandardElementConstraints(false);
		databasePool = p;
	}

	protected void init() {
		invokePreInit();

		initDefaultCharset();
		initHistoryLevel();
		initHistoryEventProducer();
		initCmmnHistoryEventProducer();
		initHistoryEventHandler();
		initExpressionManager();
		initBeans();
		initArtifactFactory();
		initFormEngines();
		initFormTypes();
		initFormFieldValidators();
		initScripting();
		//    initDmnEngine();
		initBusinessCalendarManager();
		initCommandContextFactory();
		initTransactionContextFactory();
		initCommandExecutors();
		initServices();
		initIdGenerator();
		initDeployers();
		initJobProvider();
		initBatchHandlers();
		initJobExecutor();
    initExternalTaskPriorityProvider();
		//    initDataSource();
		//    initTransactionFactory();
		//    initSqlSessionFactory();
		initIdentityProviderSessionFactory();
		initSessionFactories();
		initValueTypeResolver();
		initSerialization();
		//    initJpa();
		initDelegateInterceptor();
		initEventHandlers();
		//    initFailedJobCommandFactory();
		initProcessApplicationManager();
		initCorrelationHandler();
		initIncidentHandlers();
		initPasswordDigest();
		initDeploymentRegistration();
		initResourceAuthorizationProvider();
		initMetrics();
		initCommandCheckers();

		invokePostInit();
	}

	protected void initTransactionContextFactory() {
		if (transactionContextFactory == null) {
			transactionContextFactory = new StandaloneTransactionContextFactory();
		}
	}

	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
		return defaultCommandInterceptorsTxRequired;
	}

	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this, true));
		return defaultCommandInterceptorsTxRequired;
	}

	protected void initIdGenerator() {
		if (idGenerator == null) {
			// TODO: use hazelcast IdGenerator ?
			idGenerator = new StrongUuidGenerator();
		}
	}

	@Override
	protected void initPersistenceProviders() {
		addSessionFactory(new OrientdbSessionFactory(databasePool, variableListeners, getHistory()));
		addSessionFactory(new OrientdbPersistenceProviderFactory());
	}

	public void close() {
		super.close();
	}

	protected List<VariableListener> variableListeners;

	public List<VariableListener> getVariableListeners() {
		return variableListeners;
	}

	public void setVariableListeners(List<VariableListener> variableListeners) {
		this.variableListeners = variableListeners;
	}
}

