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

/**
 * @author Manfred Sattler
 *
 */
public class OrientdbProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

	private static Logger LOG = Logger.getLogger(OrientdbProcessEngineConfiguration.class.getName());

	protected OrientGraphFactory graphFactory;

	public static List<String> members = new ArrayList<String>();

	public static String manager = null;

	public OrientdbProcessEngineConfiguration(OrientGraphFactory f) {
		super();

		graphFactory = f;
		// explicitly disable unsupported features
		setHistory(HISTORY_FULL);

		setCmmnEnabled(false);
		setDmnEnabled(false);
		setAuthorizationEnabled(false);
		setMetricsEnabled(false);
		setJobExecutorActivate(false);
		setDbMetricsReporterActivate(false);
		setDeploymentLockUsed(false);
		setTenantCheckEnabled(false);
		System.err.println("OrientdbProcessEngineConfiguration");
		setExecutionTreePrefetchEnabled(false);
		CommandContextFactory ccf = createDefaultCommandContextFactory();
		ccf.setProcessEngineConfiguration(this);
		setCommandContextFactory(ccf);
	}

	private CommandContextFactory createDefaultCommandContextFactory() {
		return new CommandContextFactory();
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
		//    initJobProvider();
		//    initJobExecutor();
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
		if (transactionContextFactory==null) {
			transactionContextFactory = new StandaloneTransactionContextFactory();
		}
	}

	protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
		return defaultCommandInterceptorsTxRequired;
	}

	protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this, true));
		return defaultCommandInterceptorsTxRequired;
	}

	protected void initIdGenerator() {
		if(idGenerator == null) {
			// TODO: use hazelcast IdGenerator ?
			idGenerator = new StrongUuidGenerator();
		}
	}

	@Override
	protected void initPersistenceProviders() {
		addSessionFactory(new OrientdbSessionFactory(graphFactory));
		addSessionFactory(new OrientdbPersistenceProviderFactory());
	}


	public void close() {
		super.close();
	}

}
