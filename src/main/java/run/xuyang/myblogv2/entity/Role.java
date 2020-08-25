package run.xuyang.myblogv2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.List;

/**
 * 角色 实体类
 *
 * @author XuYang
 * @date 2020/8/10 13:48
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * <p>主键, 使用自增策略</p>
     * 用户 id
     */
    @TableId(type = IdType.AUTO)
    private Long rid;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 拥有该角色的所有用户
     */
    @TableField(exist = false)
    private List<User> users;
}
