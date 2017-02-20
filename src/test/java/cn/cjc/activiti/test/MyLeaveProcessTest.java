package cn.cjc.activiti.test;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author chenjc
 * @since 2017-02-20
 */
public class MyLeaveProcessTest extends AbstractTest {
    @Override
    public void process() {
        String groupName = "deptLeader";
        String userName = "deptLeaderUser";

        // 部署流程定义文件
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String processFileName = "diagram/请假流程.bpmn";
        repositoryService.createDeployment().addClasspathResource(processFileName).deploy();

        // 验证已部署流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myLeave").singleResult();
        //流程定义key就是bpmn文件中process标签的id
        assertEquals("myLeave", processDefinition.getKey());

        // 启动流程并返回流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("applyUser", "employee1");
        variables.put("days", 3);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myLeave", variables);
        assertNotNull(processInstance);
        System.out.println("pid=" + processInstance.getId() + ", pdid=" + processInstance.getProcessDefinitionId());

        //任务
        TaskService taskService = processEngine.getTaskService();
        Task task4DeptLeader = taskService.createTaskQuery().taskCandidateGroup(groupName).singleResult();
        assertNotNull(task4DeptLeader);
        assertEquals("领导审批", task4DeptLeader.getName());
        //签收
        taskService.claim(task4DeptLeader.getId(), userName);
        variables = new HashMap<>();
        variables.put("approved", true);
        //完成任务
        taskService.complete(task4DeptLeader.getId(), variables);
        task4DeptLeader = taskService.createTaskQuery().taskCandidateGroup(groupName).singleResult();
        assertNull(task4DeptLeader);
    }
}
