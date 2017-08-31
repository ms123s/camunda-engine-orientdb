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
package org.camunda.bpm.engine.impl.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.standalone.StandaloneTransactionContextFactory;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.orientdb.OrientdbPersistenceProviderFactory;
import org.camunda.bpm.engine.impl.db.orientdb.OrientdbSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.ProcessEngineException;

import java.lang.reflect.Method;

/**
 * @author Manfred Sattler
 *
 */
public class H2ProcessEngineConfiguration extends StandaloneProcessEngineConfiguration {

	private static Logger LOG = Logger.getLogger(H2ProcessEngineConfiguration.class.getName());

	public static List<String> members = new ArrayList<String>();
	protected DataSource dataSource;

	public static String manager = null;

	public H2ProcessEngineConfiguration() {
		super();

		setDatabaseSchemaUpdate("true");
		setDataSource(getDataSource("jdbc:h2:file:/tmp/h2;DB_CLOSE_DELAY=1000;TRACE_LEVEL_FILE=2"));
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
	}

	private DataSource getDataSource(String url) {
		if (this.dataSource != null){
			return this.dataSource;
		}

		org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
		ds.setUser("sa");
		ds.setPassword("");
		ds.setURL(url);
		this.dataSource = ds;
		return this.dataSource;
	}

  protected void initDmnEngine() {
  }
}

