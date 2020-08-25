package run.xuyang.myblogv2.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import run.xuyang.myblogv2.entity.User;
import run.xuyang.myblogv2.response.LayuiResponse;
import run.xuyang.myblogv2.response.ResultResponse;
import run.xuyang.myblogv2.service.RoleService;
import run.xuyang.myblogv2.service.UserService;

import java.util.*;

import static cn.hutool.core.util.StrUtil.*;

/**
 * 博客用户 接口
 *
 * @author XuYang
 * @date 2020/8/3 12:18
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v2/user")
@Slf4j
public class UserController {

    private final UserService userService;

    private final RoleService roleService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserService userService, RoleService roleService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * 验证用户的操作密码
     *
     * @param uid           用户id
     * @param operationPass 操作密码
     * @return 操作密码是否正确
     */
    @PostMapping("/verifyOperationPassword")
    public Map<String, Object> verifyOperationPassword(
            @RequestParam(name = "uid") Long uid,
            @RequestParam(name = "operationPass") String operationPass) {
        if (uid == null || uid < 0 || isBlank(operationPass)) {
            return ResultResponse.getFailResponse(500, "参数不合法!");
        }
        User user = userService.findUserByUid(uid);
        if (bCryptPasswordEncoder.matches(
                operationPass,
                user.getOperationPass())) {
            return ResultResponse.getSuccessResponse(200, "口令正确!");
        }
        return ResultResponse.getSuccessResponse(500, "口令错误!");
    }

    /**
     * 分页获取 所有 用户
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 分页后的用户集合
     */
    @GetMapping("/findUsersByPage")
    public Map<String, Object> findUsersByPage(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                               @RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        List<User> users = userService.findUsersByPage(page, limit);
        if (users != null) {
            return LayuiResponse.getSuccessResponse(
                    200,
                    "用户列表获取成功!",
                    userService.countUser(),
                    users
            );
        } else {
            return LayuiResponse.getFailResponse(
                    500,
                    "用户列表获取失败!",
                    0L,
                    null
            );
        }
    }


    /**
     * 添加一个用户
     *
     * @return 是否添加成功
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/saveUser")
    public Map<String, Object> saveUser(@RequestParam(name = "params") String params) throws JsonProcessingException {
        log.info(params);
        String[] paramList = params
                .substring(1, params.length() - 1)
                .replaceAll("\"", "")
                .split(",");
        // 前三个参数不能少, 权限参数可以不写, 但是不能大于权限的个数
        if (3 <= paramList.length && paramList.length <= roleService.countRole() + 3) {
            List<Long> roleParam = new LinkedList<>();
            String username = null, loginPass = null, operationPass = null;
            for (String param : paramList) {
                String[] kv = param.split(":");
                if (kv[0].equals("username")) {
                    username = kv[1].trim();
                } else if (kv[0].equals("loginPass")) {
                    loginPass = kv[1].trim();
                } else if (kv[0].equals("operationPass")) {
                    operationPass = kv[1].trim();
                } else if (kv[0].contains("roles")) {
                    roleParam.add(Long.valueOf(kv[1]));
                }
            }
            User user = new User(
                    null,
                    username,
                    bCryptPasswordEncoder.encode(loginPass),
                    bCryptPasswordEncoder.encode(operationPass),
                    null,
                    null);
            if (userService.addUser(user) != 0) {
                if (roleParam.size() > 0) {
                    Long uid = userService.findUserByUserName(username).getUid();
                    userService.addRolesToUser(uid, roleParam);
                }
                return ResultResponse.getSuccessResponse(200, "添加用户成功!");
            }
            return ResultResponse.getFailResponse(500, "添加用户失败!");
        } else {
            return ResultResponse.getFailResponse(500, "参数不合法!");
        }
    }

    /**
     * 批量删除用户, 同时会清空关联表信息
     *
     * @return 批量删除用户是否成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/batchDeleteUser")
    public Map<String, Object> batchDeleteUser(@RequestParam(name = "batchDelIdsJSON") String batchDelIdsJSON) throws JsonProcessingException {
        // 将 Json 数据转为 uid 的集合
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Long.class);
        List<Long> batchDelIds = objectMapper.readValue(batchDelIdsJSON, listType);
        if (batchDelIds.size() == 0) {
            return ResultResponse.getFailResponse(500, "批量删除参数错误!");
        }
        // 先根据 uid 批量删除关联表
        userService.batchDeleteUserRole(batchDelIds);
        // 批量删除用户
        if (userService.batchDeleteUser(batchDelIds) != batchDelIds.size()) {
            return ResultResponse.getFailResponse(500, "批量删除角色出错!");
        } else {
            return ResultResponse.getSuccessResponse(200, "批量删除" + batchDelIds.size() + "个用户成功!");
        }

    }

    /**
     * 根据 uid 删除 一个用户以及权限信息
     *
     * @param uid 用户id
     * @return 用户是否删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/deleteUser/{uid}")
    public Map<String, Object> deleteUser(@PathVariable(name = "uid") Long uid) {
        // 先删除该用户的权限信息
        userService.batchDeleteUserRole(new ArrayList<>(Collections.singletonList(uid)));
        // 删除用户
        if (userService.deleteUser(uid) != 0) {
            return ResultResponse.getSuccessResponse(200, "用户删除成功!");
        }
        return ResultResponse.getSuccessResponse(500, "用户删除失败!");
    }

    /**
     * 根据 uid 逻辑删除(关闭) 一个用户
     *
     * @param uid 用户id
     * @return 用户是否逻辑删除成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/logicalDeleteUser/{uid}")
    public Map<String, Object> logicalDeleteUser(@PathVariable(name = "uid") Long uid) {
        // 关闭用户
        if (userService.logicalDeleteUser(uid) != 0) {
            return ResultResponse.getSuccessResponse(200, "用户关闭成功!");
        } else {
            return ResultResponse.getSuccessResponse(500, "用户关闭失败!");
        }
    }

    /**
     * 根据 uid 开启 一个用户
     *
     * @param uid 用户id
     * @return 用户是否开启成功的信息
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/openUser/{uid}")
    public Map<String, Object> openUser(@PathVariable(name = "uid") Long uid) {
        // 关闭用户
        if (userService.openUser(uid) != 0) {
            return ResultResponse.getSuccessResponse(200, "用户开启成功!");
        } else {
            return ResultResponse.getSuccessResponse(500, "用户开启失败!");
        }
    }

    /**
     * 编辑一个用户
     *
     * @return 是否编辑成功
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/editUser")
    public Map<String, Object> editUser(
            @RequestParam(name = "uid") Long uid,
            @RequestParam(name = "params") String params) throws JsonProcessingException {
        log.info(params);
        String[] paramList = params
                .substring(1, params.length() - 1)
                .replaceAll("\"", "")
                .split(",");
        // 前三个参数不能少, 权限参数可以不写, 但是不能大于权限的个数
        if (3 <= paramList.length && paramList.length <= roleService.countRole() + 3) {
            List<Long> roleParam = new LinkedList<>();
            String username = null, loginPass = null, operationPass = null;
            for (String param : paramList) {
                String[] kv = param.split(":");
                if (kv[0].equals("username")) {
                    username = kv[1].trim();
                } else if (kv[0].equals("loginPass")) {
                    loginPass = kv[1].trim();
                } else if (kv[0].equals("operationPass")) {
                    operationPass = kv[1].trim();
                } else if (kv[0].contains("roles")) {
                    roleParam.add(Long.valueOf(kv[1]));
                }
            }
            User user = new User(
                    uid,
                    username,
                    bCryptPasswordEncoder.encode(loginPass),
                    bCryptPasswordEncoder.encode(operationPass),
                    null,
                    null);
            if (userService.editUser(user) != 0) {
                if (roleParam.size() > 0) {
                    // 删除以前的权限信息
                    userService.batchDeleteUserRole(new ArrayList<>(Collections.singletonList(uid)));
                    // 添加新的权限信息
                    userService.addRolesToUser(uid, roleParam);
                }
                return ResultResponse.getSuccessResponse(200, "修改用户成功!");
            }
            return ResultResponse.getFailResponse(500, "修改用户失败!");
        } else {
            return ResultResponse.getFailResponse(500, "参数不合法!");
        }
    }

}
