package run.xuyang.myblogv2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.omg.CORBA.INTERNAL;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import run.xuyang.myblogv2.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户 持久层接口
 *
 * @author XuYang
 * @date 2020/8/10 13:55
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据 uid 查询用户以及该用户所拥有的全部权限
     *
     * @param uid 用户id
     * @return 用户
     */
    @Results(id = "userMapWithRoles", value = {
            @Result(id = true, column = "uid", property = "uid", javaType = Long.class),
            @Result(column = "username", property = "username", javaType = String.class),
            @Result(column = "login_pass", property = "loginPass", javaType = String.class),
            @Result(column = "operation_pass", property = "operationPass", javaType = String.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class),
            @Result(column = "uid", property = "roles",
                    many = @Many(select = "run.xuyang.myblogv2.mapper.RoleMapper.findRolesByUid", fetchType = FetchType.EAGER))
    })
    @Select(" select uid, username, login_pass, operation_pass, deleted " +
            " from user " +
            " where uid = #{uid} ")
    User findUserByUid(@Param(value = "uid") Long uid);

    /**
     * 根据 用户名 查询 用户
     *
     * @param s 用户名
     * @return 用户
     */
    @ResultMap("userMapWithRoles")
    @Select(" select uid, username, login_pass, operation_pass, deleted " +
            " from user " +
            " where username = #{username} ")
    User findUserByUsername(@Param(value = "username") String s);

    /**
     * 分页查询
     *
     * @param start 起始位置
     * @param end   结束位置
     * @return 分页数据
     */
    @ResultMap("userMapWithRoles")
    @Select(" select uid, username, login_pass, operation_pass, deleted " +
            " from user " +
            " limit #{start}, #{end} ")
    List<User> findUsersByPage(@Param(value = "start") int start,
                               @Param(value = "end") int end);

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    @Select(" select count(*) from user ")
    Long countUser();

    /**
     * 向 user_role 表中给 用户id = uid 的用户添加角色列表
     *
     * @param uid       用户id
     * @param roleParam 角色列表
     * @return 影响的数据条数
     */
    int addRolesToUser(@Param(value = "uid") Long uid, @Param(value = "roleParam") List<Long> roleParam);

    /**
     * 删除 user_role 表中所有 uid 包含在 batchDelIds 中的记录
     *
     * @param batchDelIds 要删除的 uid 集合
     * @return 删除成功的条数
     */
    int batchDeleteUserRole(@Param(value = "batchDelIds") List<Long> batchDelIds);

    /**
     * 批量删除用户
     *
     * @param batchDelIds uid 集合
     * @return 删除成功的条数
     */
    int batchDeleteUser(@Param(value = "batchDelIds") List<Long> batchDelIds);

    /**
     * 根据 uid 删除用户
     *
     * @param uid 用户id
     * @return 删除成功的条数
     */
    @Delete(" delete from user where uid = #{uid} ")
    int deleteUser(@Param(value = "uid") Long uid);

    /**
     * 根据 uid 关闭用户
     *
     * @param uid 用户id
     * @return 更新成功的数据条数
     */
    @Update(" update user set deleted = 1 where uid = #{uid} ")
    int logicalDeleteUser(@Param(value = "uid") Long uid);

    /**
     * 根据 uid 开启用户
     *
     * @param uid 用户id
     * @return 更新成功的数据条数
     */
    @Update(" update user set deleted = 0 where uid = #{uid} ")
    int openUser(@Param(value = "uid") Long uid);

    /**
     * 修改用户资料
     *
     * @param user 用户
     * @return 修改成功的条数
     */
    @Update(" update user " +
            " set username = #{user.username}, login_pass = #{user.loginPass}, operation_pass = #{user.operationPass} " +
            " where uid = ${user.uid} ")
    int editUser(@Param(value = "user") User user);
}
