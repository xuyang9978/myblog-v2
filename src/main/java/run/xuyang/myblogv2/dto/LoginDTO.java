package run.xuyang.myblogv2.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 博客后台用户登录参数
 *
 * @author XuYang
 * @date 2020/7/30 18:51
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String loginPass;
}
