package run.xuyang.myblogv2.service.impl;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.entity.Role;
import run.xuyang.myblogv2.entity.User;
import run.xuyang.myblogv2.mapper.UserMapper;
import run.xuyang.myblogv2.service.UserService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户 业务层接口实现类
 *
 * @author XuYang
 * @date 2020/8/10 13:58
 */
@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        try {
            User user = userMapper.findUserByUsername(s);
            if (user == null) {
                return null;
            }
            Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
            Set<Role> roles = user.getRoles();
            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
            }
            // 这里只做账户是否可用的判断，其余的看需求设计
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getLoginPass(),
                    user.getDeleted() == 0,
                    true,
                    true,
                    true,
                    authorities);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findUserByUserName(String username) {
        return userMapper.findUserByUsername(username);
    }

    @Override
    public User findUserByUid(Long uid) {
        return userMapper.findUserByUid(uid);
    }

    @Override
    public List<User> findUsersByPage(Integer page, Integer limit) {
        int start = (page - 1) * limit;
        int end = page + limit;
        return userMapper.findUsersByPage(start, end);
    }

    @Override
    public Long countUser() {
        return userMapper.countUser();
    }

    @Override
    public int addUser(User user) {
        return userMapper.insert(user);
    }

    @Override
    public void addRolesToUser(Long uid, List<Long> roleParam) {
        userMapper.addRolesToUser(uid, roleParam);
    }

    @Override
    public void batchDeleteUserRole(List<Long> batchDelIds) {
        userMapper.batchDeleteUserRole(batchDelIds);
    }

    @Override
    public int batchDeleteUser(List<Long> batchDelIds) {
        return userMapper.batchDeleteUser(batchDelIds);
    }

    @Override
    public int deleteUser(Long uid) {
        return userMapper.deleteUser(uid);
    }

    @Override
    public int logicalDeleteUser(Long uid) {
        return userMapper.logicalDeleteUser(uid);
    }

    @Override
    public int openUser(Long uid) {
        return userMapper.openUser(uid);

    }

    @Override
    public int editUser(User user) {
        return userMapper.editUser(user);
    }

}
