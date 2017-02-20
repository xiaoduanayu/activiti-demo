package cn.cjc.activiti.test;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 测试用例启动的先决条件是：有mysql实例且有名字为activiti的数据库
 *
 * @author chenjc
 * @since 2017-02-20
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/activiti.cfg.xml")
public abstract class AbstractTest {

    @Resource
    protected ProcessEngine processEngine;

    @Test
    public abstract void process();
}
