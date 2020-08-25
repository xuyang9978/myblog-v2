package run.xuyang.myblogv2.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import run.xuyang.myblogv2.entity.User;

import java.util.List;

/**
 * 用户 业务层接口
 *
 * @author XuYang
 * @date 2020/8/10 13:57
 */
public interface UserService extends UserDetailsService {

    User findUserByUserName(String username);

    User findUserByUid(Long uid);

    List<User> findUsersByPage(Integer page, Integer limit);

    Long countUser();

    int addUser(User user);

    void addRolesToUser(Long uid, List<Long> roleParam);

    void batchDeleteUserRole(List<Long> batchDelIds);

    int batchDeleteUser(List<Long> batchDelIds);

    int deleteUser(Long uid);

    int logicalDeleteUser(Long uid);

    int openUser(Long uid);

    int editUser(User user);
}
