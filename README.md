# myblog-v2(个人博客网站 v2 版本)

- ### 博客前台

    - HTML、CSS、JQuery
    - 文章评论插件使用: [gitalk](https://github.com/gitalk/gitalk)
    - 博客地址: [徐杨的个人博客](http://www.xuyang.run/blog)

- ### 博客后台
    
    - 使用[layui](https://www.layui.com/)作为后台管理整体框架
    - 采用 Spring Boot + Thymeleaf
    - 采取 RedisTemplate 进行文章和分类数据的缓存
    - 采取 SpringBoot 自带定时任务进行缓存数据的持久化
    - 集成 Spring Security 进行用户-角色的简单权限控制, 有兴趣的可以进行二次开发
    - 编写文章使用 [editor.md](https://pandao.github.io/editor.md/) 插件
    - 持久层选用 mybatis-plus
    - 图片存储使用[七牛云](https://www.qiniu.com/)

## 如何使用
   1. 将该[项目代码](https://github.com/xuyang9978/myblog-v2.git) `clone`或下载压缩包至本地
   2. 创建数据库, 数据库名`db_blog`, 可自行修改, 同时记得修改`application.yml`文件中数据库相关的配置
   3. sql 语句在 `src\main\resources\sql\db_blog.sql`, 使用 `SQLyog`或者`navicat`运行该sql文件即可创建数据库, 里面包含一些当初的测试数据, 可以直接使用
   4. 使用七牛云存储需要修改`application.yml`中的相关配置
   5. 开启本地的`redis`,端口默认`3306`
   6. 启动项目, 打开浏览器访问`http:localhost:8864`即可访问博客前台
   7. 后台管理地址为`http:localhost:8864/admin`, 由于开启了`Spring Security`的权限管理, 所以需要进行认证通过才能进行访问, 
      解决办法为: 找到测试类`run.xuyang.myblogv2.test.EncodePass`并输入原密码进行加密,然后自己新建一个用户,并将加密出来的密码粘贴到用户表中, 
      以及在`user_role`表中给该用户添加`ADMIN`角色即可
   8. 可改善的地方: 角色是写死了的,而且没有加上权限的控制,可以根据需要自行修改开发
