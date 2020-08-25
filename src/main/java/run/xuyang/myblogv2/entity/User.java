package run.xuyang.myblogv2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;


/**
 * 用户 实体类
 *
 * @author XuYang
 * @date 2020/8/10 13:38
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <p>主键, 使用自增策略</p>
     * 用户 id
     */
    @TableId(type = IdType.AUTO)
    private Long uid;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String loginPass;

    /**
     * 操作密码
     */
    private String operationPass;

    /**
     * 用户是否被禁止登录, 0 表示可以登录, 1 表示禁止登录
     */
    private Integer deleted;

    /**
     * 用户拥有的角色列表
     */
    @TableField(exist = false)
    private Set<Role> roles;
}
