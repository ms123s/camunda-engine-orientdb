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
package org.camunda.bpm.engine.impl.hazelcast;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;

import static org.camunda.bpm.engine.variable.Variables.*;

/**
 * @author Daniel Meyer
 *
 */
public class SmokeTest extends PluggableProcessEngineTestCase {

  public void testStartToEnd() {

    deploymentId = repositoryService.createDeployment()
      .addModelInstance("process1.bpmn", Bpmn.createExecutableProcess("testProcess")
          .startEvent()
          .endEvent()
          .done())
      .deploy()
    .getId();

    runtimeService.startProcessInstanceByKey("testProcess", createVariables().putValue("foo", stringValue("bar")));

  }

  public void testWaitState() {

    String taskId = "waitForMessage";

    deploymentId = repositoryService.createDeployment()
      .addModelInstance("process1.bpmn", Bpmn.createExecutableProcess("testProcess")
          .startEvent()
          .receiveTask(taskId)
          .endEvent()
          .done())
      .deploy()
    .getId();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", createVariables().putValue("foo", stringValue("bar")));

    // verify process is waiting at task
    ActivityInstance activityInstance = runtimeService.getActivityInstance(pi.getId());
    assertTrue(activityInstance.getActivityInstances(taskId).length == 1);

    // signal process instance to finish
    runtimeService.signal(pi.getId());

  }

}
