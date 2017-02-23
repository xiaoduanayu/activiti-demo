package cn.cjc.activiti.test;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 外置表单
 *
 * @author chenjc
 * @since 2017-02-23
 */
public class LeaveFormKeyTest extends AbstractTest {
//    /**
//     * 领导驳回后申请人取消申请
//     */
//    @Test
//    @Deployment(resources = "chapter6/dynamic-form/leave.bpmn")
//    public void cancelApply() throws Exception {
//
//        // 设置当前用户
//        String currentUserId = "henryyan";
//        identityService.setAuthenticatedUserId(currentUserId);
//
//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Map<String, String> variables = new HashMap<String, String>();
//        Calendar ca = Calendar.getInstance();
//        String startDate = sdf.format(ca.getTime());
//        ca.add(Calendar.DAY_OF_MONTH, 2);
//        String endDate = sdf.format(ca.getTime());
//
//        // 启动流程
//        variables.put("startDate", startDate);
//        variables.put("endDate", endDate);
//        variables.put("reason", "公休");
//        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
//        assertNotNull(processInstance);
//
//        // 部门领导审批通过
//        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
//        variables = new HashMap<String, String>();
//        variables.put("deptLeaderApproved", "false");
//        formService.submitTaskFormData(deptLeaderTask.getId(), variables);
//
//        // 调整申请
//        Task modifyApply = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
//        variables = new HashMap<String, String>();
//        variables.put("reApply", "false");
//        variables.put("startDate", startDate);
//        variables.put("endDate", endDate);
//        variables.put("reason", "公休");
//        formService.submitTaskFormData(modifyApply.getId(), variables);
//
//        // 读取历史变量
//        Map<String, Object> historyVariables = packageVariables(processInstance);
//
//        // 验证执行结果
//        assertEquals("canceled", historyVariables.get("result"));
//
//    }

    /**
     * 读取历史变量并封装到Map中
     */
    private Map<String, Object> packageVariables(ProcessInstance processInstance, HistoryService historyService) {
        Map<String, Object> historyVariables = new HashMap<>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
        for (HistoricDetail historicDetail : list) {
            if (historicDetail instanceof HistoricFormProperty) {
                // 表单中的字段
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                System.out.println("form field: taskId=" + field.getTaskId() + ", " + field.getPropertyId() + " = " + field.getPropertyValue());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricVariableUpdate variable = (HistoricVariableUpdate) historicDetail;
                historyVariables.put(variable.getVariableName(), variable.getValue());
                System.out.println("variable: " + variable.getVariableName() + " = " + variable.getValue());
            }
        }
        return historyVariables;
    }

    @Override
    public void process() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        InputStream inputStream = getClass().getResourceAsStream("/diagram/waizhibiaodan.zip");
        repositoryService.createDeployment().addZipInputStream(new ZipInputStream(inputStream)).deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        // 读取启动表单
        FormService formService = processEngine.getFormService();
        Object renderedStartForm = formService.getRenderedStartForm(processDefinition.getId());
        assertNotNull(renderedStartForm);

        // 启动流程
        // 设置当前用户
        IdentityService identityService = processEngine.getIdentityService();
        identityService.setAuthenticatedUserId("xiaoduanayu");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        // 部门领导审批通过
        TaskService taskService = processEngine.getTaskService();
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(formService.getRenderedTaskForm(deptLeaderTask.getId()));
        //签收
        taskService.claim(deptLeaderTask.getId(), "chenjunchi");
        variables = new HashMap<>();
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过
        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hrLeader").singleResult();
        assertNotNull(formService.getRenderedTaskForm(hrTask.getId()));
        //签收
        taskService.claim(hrTask.getId(), "wangjunmei");
        variables = new HashMap<>();
        variables.put("hrLeaderApproved", "true");
        formService.submitTaskFormData(hrTask.getId(), variables);

        // 销假（根据申请人的用户ID读取）
        Task reportBackTask = taskService.createTaskQuery().taskAssignee("xiaoduanayu").singleResult();
        assertNotNull(formService.getRenderedTaskForm(reportBackTask.getId()));
        variables = new HashMap<>();
        variables.put("reportBackDate", sdf.format(ca.getTime()));
        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束
        HistoryService historyService = processEngine.getHistoryService();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
        assertNotNull(historicProcessInstance);

        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance, historyService);

        // 验证执行结果
        assertEquals("ok", historyVariables.get("result"));
    }
}
