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

package org.camunda.bpm.engine.impl.orientdb;

import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.List;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Danny Gräf
 */
public class MessageEventSubprocessTest extends PluggableProcessEngineTestCase {

  @Override
  protected void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
    }
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }



	@Deployment(resources = { "MessageEventSubprocessTest.testNonInterruptingWithReceiveTask.bpmn20.xml" })
  public void testNonInterruptingWithReceiveTask() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
System.err.println("processInstanceId:"+processInstanceId);
System.out.println(" processInstanceId:"+processInstanceId);

    // when (1)
//    runtimeService.correlateMessage("firstMessage");
     runtimeService.messageEventReceived("firstMessage", processInstanceId);

    // then (1)
    assertEquals(2, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    // check that the parent execution of the event sub process task execution is the event
    // sub process execution
/*    assertThat(executionTree)
        .matches(
          describeExecutionTree(null).scope()
            .child(null).concurrent().noScope()
              .child("userTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
            .done());*/

    // when (2)
//    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("userTask")
        .singleResult();
    assertNotNull(task2);

    executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    // check that the parent execution of the event sub process task execution is the event
    // sub process execution
    assertThat(executionTree)
        .matches(
          describeExecutionTree(null).scope()
            .child("userTask").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
            .done());

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);
  }

  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }

    try {

        super.runBare();

    } finally {

      //identityService.clearAuthentication();
      //processEngineConfiguration.setTenantCheckEnabled(true);

      deleteDeployments();

      //deleteHistoryCleanupJob();

      closeDownProcessEngine();
      clearServiceReferences();
    }
  }
}
