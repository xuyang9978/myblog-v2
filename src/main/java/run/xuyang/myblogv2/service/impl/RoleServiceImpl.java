package run.xuyang.myblogv2.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.entity.Role;
import run.xuyang.myblogv2.mapper.RoleMapper;
import run.xuyang.myblogv2.service.RoleService;

import java.util.List;

/**
 * 角色 业务层接口实现类
 *
 * @author XuYang
 * @date 2020/8/10 13:58
 */
@Service("roleService")
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> findAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public long countRole() {
        return roleMapper.selectCount(null);
    }
}
