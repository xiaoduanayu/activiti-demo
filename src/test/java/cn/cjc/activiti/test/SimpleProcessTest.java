package cn.cjc.activiti.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 测试用例启动的先决条件是：有mysql实例且有名字为activiti的数据库
 *
 * @author chenjc
 * @since 2017-02-16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/activiti.cfg.xml")
public class SimpleProcessTest {

    @Resource
    private ProcessEngine processEngine;

    @Test
    public void testStartProcess() throws Exception {
        // 部署流程定义文件
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String processFileName = "diagram/simple.bpmn";
        repositoryService.createDeployment().addClasspathResource(processFileName).deploy();

        // 验证已部署流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        //流程定义key就是bpmn文件中process标签的id
        assertEquals("leave", processDefinition.getKey());

        // 启动流程并返回流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave");
        assertNotNull(processInstance);
        System.out.println("pid=" + processInstance.getId() + ", pdid=" + processInstance.getProcessDefinitionId());
    }
}