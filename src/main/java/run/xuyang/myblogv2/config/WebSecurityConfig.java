package run.xuyang.myblogv2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import run.xuyang.myblogv2.handler.CustomAccessDeniedHandler;
import run.xuyang.myblogv2.service.UserService;

/**
 * Spring Security 配置
 *
 * @author XuYang
 * @date 2020/8/10 19:27
 */
@EnableWebSecurity
// 开启 Spring Security 方法级安全注解 @EnableGlobalMethodSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 自定义的用户认证业务对象
     */
    private final UserService userService;

    /**
     * 自定义处理无权请求处理器对象
     */
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * 将加密对象交给 Spring IOC 容器管理
     *
     * @return 默认加密对象
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public WebSecurityConfig(UserService userService, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.userService = userService;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    /**
     * 认证 配置
     *
     * @param auth auth
     * @throws Exception exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用数据库中的用户数据进行认证
        auth
                .userDetailsService(userService)
                .passwordEncoder(bCryptPasswordEncoder());

        // 从内存中创建对象进行认证
//        auth
//                .inMemoryAuthentication()
//                .passwordEncoder(bCryptPasswordEncoder())
//                .withUser("admin")
//                .password(bCryptPasswordEncoder().encode("admin"))
//                .roles("admin")
//                .and()
//                .withUser("user")
//                .password("{noop}user") // 加上 {noop} 可以不进行加密使用
//                .roles("user");
    }

    /**
     * 静态资源配置
     *
     * @param web web
     * @throws Exception exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 不拦截静态资源,所有用户均可访问的资源
        web
                .ignoring()
                .antMatchers(
                        "/css/**",
                        "/fonts/**",
                        "/img/**",
                        "/js/**",
                        "/plugins/**");
    }

    /**
     * 授权( http 请求 ) 配置
     *
     * @param http http
     * @throws Exception exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //解决 in a frame because it set 'X-Frame-Options' to 'DENY' 问题
        http
                .headers()
                .frameOptions()
                .disable();

        // 指定资源拦截规则
        http
                .authorizeRequests()
                .antMatchers(
                        "/",
                        "/index",
                        "/blog",
                        "/article",
                        "/category",
                        "/more-article",
                        "/search").permitAll()                 // 博客前台页面全部放行
                .antMatchers(
                        "/404",
                        "/500").permitAll()                    // 异常页面放行
                .antMatchers("/login").permitAll()  // 登录页面放行
                .anyRequest().authenticated();                 // 其余所有请求需要认证通过才能访问

        // 指定自定义登录认证页面
        http
                .formLogin()
                .loginPage("/login")                    // 自定义登录页面
                .loginProcessingUrl("/login")           // 登录 POST 请求路径
                .failureForwardUrl("/login?error=1")    // 登录失败的路径
                .defaultSuccessUrl("/admin")            // 登录成功后跳转的页面
                .usernameParameter("username")          // 登录用户名参数
                .passwordParameter("loginPass");        // 登录密码参数

        // 开启记住登录功能, 默认有效期 两周
        http
                .rememberMe()
                .tokenValiditySeconds(2 * 60 * 60)  // 设置 cookie 有效期两小时
                .rememberMeParameter("remember");

        // 指定退出认证配置
        http
                .logout()
                .logoutSuccessUrl("/login?logout=1"); // 退出登录成功 URL

        // 处理无权请求
        http
                .exceptionHandling()
                .accessDeniedHandler(customAccessDeniedHandler);

        // 关闭 csrf 配置
        http
                .csrf()
                .disable();
    }
}
