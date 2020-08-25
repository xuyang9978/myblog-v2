package run.xuyang.myblogv2.controller;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.xuyang.myblogv2.cache.ArticleCache;
import run.xuyang.myblogv2.cache.CategoryCache;
import run.xuyang.myblogv2.cache.LinkCache;
import run.xuyang.myblogv2.dto.ArticlePageDTO;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.entity.Link;

import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.StrUtil.isBlank;
import static run.xuyang.myblogv2.util.ArticleUtils.subString;

/**
 * 博客前台页面跳转 控制器
 *
 * @author XuYang
 * @date 2020/7/20 14:05
 */
@Controller
public class PageController {

    private final ArticleCache articleCache;

    private final CategoryCache categoryCache;

    private final LinkCache linkCache;

    public PageController(ArticleCache articleCache, CategoryCache categoryCache, LinkCache linkCache) {
        this.articleCache = articleCache;
        this.categoryCache = categoryCache;
        this.linkCache = linkCache;
    }


    /**
     * 跳转到 博客首页
     *
     * @return pages/blog
     */
    @RequestMapping("/blog")
    public String toBlog(Model model) {
        // 倒序获取最近的博客文章以及分类信息
        List<Article> articles = articleCache.reverseGetAllOpenedArticlesWithCategory();
        // 最近发表的一篇文章
        Article latestArticle = articles.get(0);
        // 该文章的简介
        String intro = subString(latestArticle.getHtmlContent());
        // 所有开启中的友链
        List<Link> links = linkCache.getAllLinks()
                .stream()
                .filter(link -> link.getDeleted() == 0)
                .collect(Collectors.toList());

        model.addAttribute("articles", articles.subList(0, Math.min(15, articles.size())));
        model.addAttribute("latestArticle", latestArticle);
        model.addAttribute("intro", intro);
        model.addAttribute("articleCount", articles.size());
        model.addAttribute("links", links);
        return "pages/blog";
    }

    /**
     * 跳转到 网站首页
     *
     * @return index
     */
    @RequestMapping("/index")
    public String toIndex() {
        return "index";
    }

    /**
     * 跳转到 分类页面
     *
     * @return pages/category
     */
    @RequestMapping("/category")
    public String toCategory(Model model, @RequestParam(name = "cid") Long cid) {
        Category curCategory = categoryCache.getCategoryByCidWithArticles(cid);
        // 如果 该分类不存在 或者 该分类已被关闭
        if (curCategory == null || curCategory.getDeleted() == 1) {
            return "error/404";
        }
        Map<Long, List<Article>> articlesPerYear = new LinkedHashMap<>();
        List<Article> articles = articleCache.getArticlesByCid(curCategory.getCid());

        // 将一个分类下的所有文章按照发布的 年份 进行分组
        this.classifyByYear(articlesPerYear, articles);

        // 所有的分类
        List<Category> categories = categoryCache.getAllOpenedCategoriesWithArticles();

        model.addAttribute("curCategory", curCategory);
        model.addAttribute("articlesPerYear", articlesPerYear);
        model.addAttribute("categories", categories);

        return "pages/category";
    }

    /**
     * 跳转到 更多文章页面
     *
     * @return pages/more-article
     */
    @RequestMapping("/more-article")
    public String toMoreArticle(Model model, ArticlePageDTO articlePageDTO) {
        // 分页后的文章集合
        List<Article> articles = articleCache.getOpenedArticlesByPageWithCategory(articlePageDTO.getPage(), articlePageDTO.getLimit());
        // 获取最新一篇文章
        Article latestArticle = articleCache.getLatestOneArticle();
        // 该文章的简介
        String intro = subString(latestArticle.getHtmlContent());
        // 博客开启中的总文章数
        Long articleCount = articleCache.getOpenedCount();
        // 所有的分类
        List<Category> categories = categoryCache.getAllOpenedCategoriesWithArticles();
        // 计算总页数
        long totalPage = (articleCount + articlePageDTO.getLimit() - 1) / articlePageDTO.getLimit();

        model.addAttribute("articles", articles);
        model.addAttribute("latestArticle", latestArticle);
        model.addAttribute("intro", intro);
        model.addAttribute("articleCount", articleCount);
        model.addAttribute("categories", categories);
        model.addAttribute("articlePageDTO", articlePageDTO);
        model.addAttribute("totalPage", totalPage);

        return "pages/more-article";
    }

    /**
     * 跳转到 文章详情页面
     *
     * @return pages/article
     */
    @RequestMapping("/article")
    public String toArticle(Model model, @RequestParam(name = "aid") Long aid) {
        Article article = articleCache.getArticleByIdWithCategory(aid);
        // 如果文章不存在或者已经被关闭
        if (article == null || article.getDeleted() == 1) {
            return "error/404";
        }
        // 获取当前文章的前一篇文章
        Article prevArticle = articleCache.findPrevArticleWithCategoryById(aid);
        // 获取当前文章的后一篇文章
        Article nextArticle = articleCache.findNextArticleWithCategoryById(aid);

        model.addAttribute("article", article);
        model.addAttribute("prevArticle", prevArticle);
        model.addAttribute("nextArticle", nextArticle);
        return "pages/article";
    }

    /**
     * 将searchCondition分别根据文章标题/作者名字/文章分类名来进行模糊查询,
     * 然后将所有的文章做成一个列表显示出来
     *
     * @param searchCondition 查询名字
     * @param model           model
     * @return 名字包含name的文章
     */
    @RequestMapping("/search")
    public String searchArticle(
            Model model,
            @RequestParam(name = "searchCondition") String searchCondition) {

        List<Article> titleMatchedArticles = new LinkedList<>();
        List<Article> authorMatchedArticles = new LinkedList<>();
        Article randomOneArticle = null;

        // 先判断参数是否为空, 如果为空则随机推荐一篇文章
        if (isBlank(searchCondition)) {
            randomOneArticle = articleCache.randomOneArticle();
            // 该文章的简介
            String intro = subString(randomOneArticle.getHtmlContent());
            model.addAttribute("intro", intro);
        } else {
            // 参数不为空则按照参数进行模糊查询
            // 文章标题满足条件的文章
            titleMatchedArticles = articleCache.findAllOpenedArticlesByTitle(searchCondition);
            Collections.reverse(titleMatchedArticles);
            // 作者名字满足条件的文章
            authorMatchedArticles = articleCache.findAllOpenedArticlesByAuthor(searchCondition);
            Collections.reverse(authorMatchedArticles);
        }

        // 所有的分类
        List<Category> categories = categoryCache.getAllOpenedCategoriesWithArticles();

        model.addAttribute("titleMatchedArticles", titleMatchedArticles);
        model.addAttribute("authorMatchedArticles", authorMatchedArticles);
        model.addAttribute("randomOneArticle", randomOneArticle);
        model.addAttribute("keywords", searchCondition);
        model.addAttribute("categories", categories);

        return "pages/search";
    }

    /**
     * 跳转到 后台管理系统页面
     *
     * @return admin/index
     */
    @RequestMapping("/admin")
    public String toAdminIndex() {
        return "admin/index";
    }

    /**
     * 跳转到 404页面
     *
     * @return error/404
     */
    @RequestMapping("/404")
    public String to404() {
        return "error/404";
    }

    /**
     * 跳转到 500页面
     *
     * @return error/500
     */
    @RequestMapping("/500")
    public String to50X() {
        return "error/500";
    }


    //********************private zone**********************

    /**
     * 将一个分类下的所有文章按照发布的 年份 进行分组
     *
     * @param articlesPerYear 存储分组后的结果
     * @param articles        要分组的文章列表
     */
    private void classifyByYear(Map<Long, List<Article>> articlesPerYear, List<Article> articles) {
        // 存储文章的发布年份
        List<Long> createdTimeYears = new LinkedList<>();
        // 遍历文章，获取年份列表
        for (Article article : articles) {
            //获取文章的发布年份
            long createdTimeYear = DateUtil.year(article.getCreatedTime());
            //如果文章年份列表中不包含该年份
            if (!createdTimeYears.contains(createdTimeYear)) {
                //将该年份加入年份列表
                createdTimeYears.add(createdTimeYear);
            }
        }
        // 遍历刚才获取到的时间列表
        for (Long createdTimeYear : createdTimeYears) {
            // 存储一年的所有文章
            List<Article> articleList = new LinkedList<>();
            // 再次遍历文章进行判断获取属于某一年份的文章列表
            for (Article article : articles) {
                // 如果该文章的发布年份等于了该年份
                if (createdTimeYear == DateUtil.year(article.getCreatedTime())) {
                    // 则将该文章加入该年份下的文章列表
                    articleList.add(article);
                }
            }
            // 将该年的文章列表倒置，使最新的文章在最前面
            Collections.reverse(articleList);
            // 找完一年的后将该年的文章列表以及发布年份存入Map
            articlesPerYear.put(createdTimeYear, articleList);
        }
    }
}
