package com.saint.dynamic;

import com.saint.dynamic.mapper.UserMapper;
import com.saint.dynamic.model.MybatisTraceContext;
import com.saint.dynamic.model.UserDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest(classes = {MybatisDynamicPluginApplication.class, MybatisDynamicPluginApplicationTests.class})
@RunWith(SpringRunner.class)
public class MybatisDynamicPluginApplicationTests {

    @Autowired
    private UserMapper userMapper;

    /**
     * 插入一条，和MyBatis-Plugin中要改变的字段没有任何交集
     */
    @Test
    public void insert() {
        // mock traceContext内容
        mockTraceContext();
        UserDTO userDTO = new UserDTO();
        userDTO.setAge(18);
        userDTO.setIdCard("220210");
        userDTO.setEmail("238484@163.com");
        userDTO.setUserName("张三丰2");
        int result = userMapper.insert(userDTO);
        System.out.println(result);

    }

    /**
     * 插入一条数据，并且方法中传入的参数和MyBatis-Plugin中要改变的字段存在重合
     * 以MyBatis-Plugin中的数据为准
     */
    @Test
    public void insertWithExistedField() {
        // mock traceContext内容
        mockTraceContext();
        UserDTO userDTO = new UserDTO();
        userDTO.setAge(19);
        userDTO.setEmail("238484@qq.com");
        userDTO.setIdCard("220210");
        userDTO.setUserName("张三丰2");
        // 插入数据的operator_id字段应为123，但实际为2333（被traceContext中的operator_id覆盖）
        userDTO.setOperatorId(123L);
        int result = userMapper.insert(userDTO);
        System.out.println(result);
    }

    /**
     * 批量插入
     */
    @Test
    public void creates() {
        // mock traceContext内容
        mockTraceContext();
        UserDTO userDTO = new UserDTO();
        userDTO.setAge(33);
        userDTO.setIdCard("220210");
        userDTO.setEmail("238484@qq.com");
        userDTO.setUserName("张三丰");


        UserDTO userDTO2 = new UserDTO();
        userDTO2.setAge(34);
        userDTO2.setIdCard("220210");
        userDTO2.setEmail("23848422@qq.com");
        userDTO2.setUserName("张三丰2");

        List<UserDTO> users = new ArrayList<>();
        users.add(userDTO);
        users.add(userDTO2);

        int result = userMapper.creates(users);
        System.out.println(result);
    }

    /**
     * 单纯的更新，和MyBatis-Plugin中要改变的字段没有任何交集
     */
    @Test
    public void update() {
        // mock traceContext内容
        mockTraceContext();
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("SaintMm");
        userDTO.setEmail("update-email");
        userDTO.setId(6);
        userMapper.update(userDTO);
    }

    /**
     * 更新数据，并且方法中传入的参数和MyBatis-Plugin中要改变的字段存在重合
     * 以MyBatis-Plugin中的数据为准
     */
    @Test
    public void updateWithExistedField() {
        // mock traceContext内容
        mockTraceContext();
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("Bob");
        // 插入数据的operator_id字段应为123，但实际为2333（被traceContext中的operator_id覆盖）
        userDTO.setOperatorId(123L);
        userDTO.setId(6);
        userMapper.update(userDTO);
    }

    private void mockTraceContext() {
        MybatisTraceContext.TraceContext traceContext = new MybatisTraceContext.TraceContext()
                .setControllerAction("mock-test-action")
                .setOperatorId(2333L)
                .setTraceId(UUID.randomUUID().toString().replaceAll("-", ""))
                .setAppName("h5")
                .setVisitIp("192.168.1.1");
        MybatisTraceContext.setTraceContext(traceContext);
    }


}
