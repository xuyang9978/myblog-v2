package run.xuyang.myblogv2.test.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import run.xuyang.myblogv2.entity.Role;
import run.xuyang.myblogv2.entity.User;
import run.xuyang.myblogv2.mapper.RoleMapper;
import run.xuyang.myblogv2.mapper.UserMapper;

/**
 * 测试 UserMapper 和 RoleMapper
 *
 * @author XuYang
 * @date 2020/8/10 14:28
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserAndRoleMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Test
    public void test1() {
        User userByUid = userMapper.findUserByUid(1L);
        System.out.println(userByUid.getUsername());
        System.out.println("================");
        for (Role role : userByUid.getRoles()) {
            System.out.println(role.getRoleName());
        }
    }

    @Test
    public void test2() {
        Role roleByRid = roleMapper.findRoleByRid(1L);
        System.out.println(roleByRid.getRoleName());
        System.out.println("=========================");
        for (User user : roleByRid.getUsers()) {
            System.out.println(user.getUsername());
        }
    }

    @Test
    public void test3() {
        User userByUid = userMapper.findUserByUsername("wangwu");
        System.out.println(userByUid.getLoginPass());
        System.out.println("================");
        for (Role role : userByUid.getRoles()) {
            System.out.println(role.getRoleName());
        }
    }
}
