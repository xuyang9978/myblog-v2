package run.xuyang.myblogv2.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.xuyang.myblogv2.cache.ArticleCache;
import run.xuyang.myblogv2.cache.CategoryCache;
import run.xuyang.myblogv2.cache.LinkCache;
import run.xuyang.myblogv2.entity.Role;
import run.xuyang.myblogv2.entity.User;
import run.xuyang.myblogv2.service.RoleService;
import run.xuyang.myblogv2.service.UserService;
import run.xuyang.myblogv2.util.QiNiuYunUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.StrUtil.isBlank;


/**
 * 后台管理系统的页面跳转控制器
 *
 * @author XuYang
 * @date 2020/7/20 18:20
 */
@Controller
@Slf4j
public class AdminController {

    private final QiNiuYunUtils qiNiuYunUtils;

    private final ArticleCache articleCache;

    private final CategoryCache categoryCache;

    private final LinkCache linkCache;

    private final UserService userService;

    private final RoleService roleService;

    public AdminController(QiNiuYunUtils qiNiuYunUtils, ArticleCache articleCache, CategoryCache categoryCache, LinkCache linkCache, UserService userService, RoleService roleService) {
        this.qiNiuYunUtils = qiNiuYunUtils;
        this.articleCache = articleCache;
        this.categoryCache = categoryCache;
        this.linkCache = linkCache;
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * 跳转到 文章管理 页面
     *
     * @return admin/home/article/article-management
     */
    @RequestMapping("/home/article/article-management")
    public String toArticleManagement(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.findUserByUserName(username);
            model.addAttribute("user", user);
        }
        // 获取所有 分类
        model.addAttribute("allCategories", categoryCache.getAllCategories());
        return "admin/home/article/article-management";
    }


    /**
     * 跳转到 写博客 页面
     *
     * @param model model
     * @return admin/home/article/write-blog
     */
    @RequestMapping("/home/article/write-blog")
    public String toWriteArticle(Model model) {
        // 获取所有 分类
        model.addAttribute("allCategories", categoryCache.getAllCategories());
        return "admin/home/article/write-blog";
    }

    /**
     * 跳转到 编辑博客 页面
     *
     * @param aid   要被编辑的 文章的id
     * @param model model
     * @return admin/home/article/edit-blog
     */
    @RequestMapping("/home/article/edit-blog")
    public String toEditArticle(@RequestParam(value = "aid") Long aid, Model model) {
        // 获取所有 分类
        model.addAttribute("allCategories", categoryCache.getAllCategories());
        // 获取 要被编辑的 文章
        model.addAttribute("article", articleCache.getArticleById(aid));
        return "admin/home/article/edit-blog";
    }

    /**
     * 跳转到 分类管理 页面
     *
     * @return admin/home/category/category-management
     */
    @RequestMapping("/home/category/category-management")
    public String toCategoryManagement(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.findUserByUserName(username);
            model.addAttribute("user", user);
        }
        return "admin/home/category/category-management";
    }

    /**
     * 跳转到 添加分类 页面
     *
     * @return admin/home/category/add-category
     */
    @RequestMapping("/home/category/add-category")
    public String toAddCategory() {
        return "admin/home/category/add-category";
    }

    /**
     * 打开 编辑分类 窗口
     *
     * @return admin/home/category/edit-category
     */
    @RequestMapping("/home/category/edit-category")
    public String toEditCategory() {
        return "admin/home/category/edit-category";
    }

    /**
     * 跳转到 用户管理页面
     *
     * @return admin/sys/user/user-management
     */
    @RequestMapping("/sys/user/user-management")
    public String toUserManagement(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.findUserByUserName(username);
            model.addAttribute("user", user);
        }
        return "admin/sys/user/user-management";
    }

    /**
     * 跳转到添加用户页面
     *
     * @param model model
     * @return admin/sys/user/add-user
     */
    @RequestMapping("/sys/user/add-user")
    public String toAddUser(Model model) {
        model.addAttribute("roles", roleService.findAllRoles());
        return "admin/sys/user/add-user";
    }

    /**
     * 跳转到编辑用户页面
     *
     * @param model model
     * @return admin/sys/user/edit-user
     */
    @RequestMapping("/sys/user/edit-user/{uid}")
    public String toAddUser(Model model, @PathVariable Long uid) {
        User user = userService.findUserByUid(uid);
        List<Long> rids = user.getRoles()
                .stream()
                .map(Role::getRid)
                .collect(Collectors.toList());
        model.addAttribute("user", user);
        model.addAttribute("rids", rids);
        model.addAttribute("roles", roleService.findAllRoles());
        return "admin/sys/user/edit-user";
    }

    /**
     * 跳转到友链管理页面
     *
     * @param model model
     * @return admin/sys/link/link-management
     */
    @RequestMapping("/sys/link/link-management")
    public String toLinkManagement(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.findUserByUserName(username);
            model.addAttribute("user", user);
        }
        return "admin/sys/link/link-management";
    }

    /**
     * 跳转到添加友链页面
     *
     * @return admin/sys/link/add-link
     */
    @RequestMapping("/sys/link/add-link")
    public String toAddLink() {
        return "admin/sys/link/add-link";
    }

    /**
     * 跳转到编辑友链页面
     *
     * @return admin/sys/link/edit-link
     */
    @RequestMapping("/sys/link/edit-link/{lid}")
    public String toEditLink(Model model, @PathVariable(name = "lid") Long lid) {
        model.addAttribute("link", linkCache.getLinkById(lid));
        return "admin/sys/link/edit-link";
    }


    /**
     * 前往 登录页面
     *
     * @return login
     */
    @RequestMapping("/login")
    public String toLogin(
            Model model,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout) {
        if (!isBlank(error)) {
            model.addAttribute("error", "账号/密码错误,或者账号已被冻结!");
        }
        if (!isBlank(logout)) {
            model.addAttribute("logout", "退出登录成功!");
        }
        return "admin/login";
    }

    /**
     * editor.md 上传图片
     *
     * @param file 要上传的图片
     * @return 上传结果
     */
    @PostMapping("/home/article/uploadImg")
    @ResponseBody
    public Map<String, Object> uploadPictures(
            @RequestParam(value = "editormd-image-file", required = false) MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        if (file != null) {
            try {
                FileInputStream fileInputStream = (FileInputStream) file.getInputStream();
                // 将图片上传到七牛云, 并获取上传成功后的图片外链
                String url = qiNiuYunUtils.uploadQiniuYun(fileInputStream);

                res.put("url", "http://" + url);
                res.put("success", 1);
                res.put("message", "上传成功!");
            } catch (IOException e) {
                res.put("success", 0);
                res.put("message", "上传失败!");
                e.printStackTrace();
            }
        } else {
            res.put("success", 0);
            res.put("message", "没有选择文件上传!");
        }
        return res;
    }
}
