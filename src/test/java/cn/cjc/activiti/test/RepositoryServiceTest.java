package cn.cjc.activiti.test;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.Assert;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * 一次部署多个流程
 *
 * @author chenjc
 * @since 2017-02-22
 */
public class RepositoryServiceTest extends AbstractTest {
    @Override
    public void process() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        InputStream inputStream = getClass().getResourceAsStream("/diagram/multiprocess.zip");
        Deployment deploy = repositoryService.createDeployment().addZipInputStream(new ZipInputStream(inputStream)).deploy();
        System.out.println(deploy.getId());
        long count = repositoryService.createProcessDefinitionQuery().count();
        Assert.assertEquals(6, count);
    }
}
