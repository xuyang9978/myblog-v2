package run.xuyang.myblogv2.service;

import run.xuyang.myblogv2.entity.Role;

import java.util.List;

/**
 * 角色 业务层接口
 *
 * @author XuYang
 * @date 2020/8/10 13:57
 */
public interface RoleService {

    List<Role> findAllRoles();

    long countRole();
}
