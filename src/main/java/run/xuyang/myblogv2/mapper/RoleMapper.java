package run.xuyang.myblogv2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import run.xuyang.myblogv2.entity.Role;

import java.util.List;
import java.util.Set;

/**
 * 角色 持久层接口
 *
 * @author XuYang
 * @date 2020/8/10 13:56
 */
@Repository
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据 uid 查询所有的角色
     *
     * @param uid 用户id
     * @return 该用户所拥有的所有角色
     */
    @Results(id = "roleMapWithoutUsers", value = {
            @Result(id = true, column = "rid", property = "rid", javaType = Long.class),
            @Result(column = "role_name", property = "roleName", javaType = String.class),
            @Result(column = "role_desc", property = "roleDesc", javaType = String.class)
    })
    @Select(" select r.rid, r.role_name, r.role_desc " +
            " from role r " +
            " left outer join user_role ur on r.rid = ur.rid " +
            " left outer join user u on u.uid = ur.uid " +
            " where u.uid = #{uid} ")
    Set<Role> findRolesByUid(@Param(value = "uid") Long uid);

    /**
     * 根据 rid 查询角色
     *
     * @param rid 角色id
     * @return 角色
     */
    @ResultMap("roleMapWithoutUsers")
    @Select(" select rid, role_name, role_desc " +
            " from role " +
            " where rid = #{rid} ")
    Role findRoleByRid(@Param(value = "rid") Long rid);
}
