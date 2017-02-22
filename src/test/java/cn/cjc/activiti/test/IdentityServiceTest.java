package cn.cjc.activiti.test;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.junit.Test;

/**
 * 创建用户和组、建立用户和组关系
 *
 * @author chenjc
 * @since 2017-02-21
 */
public class IdentityServiceTest extends AbstractTest {

    /**
     * 创建用户
     */
    @Override
    public void process() {
        IdentityService identityService = processEngine.getIdentityService();
        User user = identityService.newUser("20081204009");
        user.setFirstName("陈");
        user.setLastName("骏驰");
        user.setEmail("xiaoduanayu-love@163.com");
        identityService.saveUser(user);
    }

    /**
     * 创建组
     */
    @Test
    public void createGroup() {
        IdentityService identityService = processEngine.getIdentityService();
        Group group = identityService.newGroup("deptLeader");
        group.setName("部门领导");
        group.setType("assignment");
        identityService.saveGroup(group);
    }

    /**
     * 创建用户和组的关联
     */
    @Test
    public void createMembership() {
        IdentityService identityService = processEngine.getIdentityService();
        identityService.createMembership("20081204009", "deptLeader");
    }
}
