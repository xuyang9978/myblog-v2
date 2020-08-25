package run.xuyang.myblogv2.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import run.xuyang.myblogv2.dto.ArticlePageSearchDTO;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.service.ArticleService;
import run.xuyang.myblogv2.util.RedisUtils;

import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.StrUtil.isBlank;
import static run.xuyang.myblogv2.util.TypeConvertUtils.objSetToTList;

/**
 * 文章缓存
 *
 * @author XuYang
 * @date 2020/8/8 16:40
 */
@Component
@Slf4j
public class ArticleCache implements ApplicationRunner {

    private final ArticleService articleService;

    private final RedisUtils redisUtils;

    // 为了解决循环依赖, 使用 @Autowired 完全交给 Spring 处理
    @Autowired
    private CategoryCache categoryCache;

    public ArticleCache(ArticleService articleService, RedisUtils redisUtils) {
        this.articleService = articleService;
        this.redisUtils = redisUtils;
    }

    private static final String articleKey = "articleCache";

    /**
     * 项目启动时将文章数据加载入缓存
     *
     * @param args 参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 从数据库中读取所有文章进行缓存
        log.info("正在缓存文章...");
        // 这里有个坑, key 里面数据量太大删除 key 可能会阻塞比较长的时间
        redisUtils.delete(articleKey);
        List<Article> allArticlesWithoutCategory = articleService.findAllArticlesWithoutCategory();
        // 将 文章id 作为 score, 文章 作为元素存储在有序列表中
        for (Article article : allArticlesWithoutCategory) {
            redisUtils.zAdd(articleKey, article, article.getAid());
        }
        log.info("缓存文章完成...");
    }

    /**
     * 返回所有文章
     *
     * @return 所有文章
     */
    public List<Article> getAllArticles() {
        log.info("正在获取所有文章...");
        List<Article> articleCache = new LinkedList<>();
        objSetToTList(redisUtils.zRange(articleKey, 0, -1), articleCache);
        log.info("获取所有文章成功...");
        return articleCache;
    }

    /**
     * 获取 文章总数
     *
     * @return 文章总数
     */
    public long getCount() {
        log.info("正在获取文章总数...");
        return redisUtils.zSize(articleKey);
    }

    /**
     * 获取分页数据
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 分页数据
     */
    public List<Article> getArticlesByPage(Integer page, Integer limit) {
        log.info("正在获取第 " + page + " 页的 " + limit + "条数据...");
        int start = (page - 1) * limit;
        int end = page * limit;
        List<Article> articleCache = this.getAllArticles();

        if (start > articleCache.size()) {
            return null;
        } else if (limit > articleCache.size()) {
            return articleCache;
        } else if (articleCache.size() >= end) {
            return articleCache.subList(start, end);
        } else {
            return articleCache.subList(start, articleCache.size());
        }
    }

    /**
     * 向缓存中插入一篇文章
     *
     * @param article 文章
     */
    public void insert(Article article) {
        log.info("正在向缓存中插入文章...");
        article.setDeleted(0);
        redisUtils.zAdd(articleKey, article, article.getAid());
        log.info("向缓存中插入文章完成...");
    }

    /**
     * 逻辑删除一篇文章
     *
     * @param aid 文章id
     */
    public void logicalDeleteArticle(Long aid) {
        log.info("正在逻辑删除文章...");
        this.changeArticleStatus(aid, 1);
        log.info("逻辑删除文章成功...");
    }

    /**
     * 批量逻辑删除文章
     *
     * @param articleIds 文章id集合
     */
    public void batchLogicDeleteArticles(List<Long> articleIds) {
        log.info("批量逻辑删除文章开始...");
        for (Long aid : articleIds) {
            this.logicalDeleteArticle(aid);
        }
        log.info("批量逻辑删除文章完成...");
    }

    /**
     * 将指定 aid 的文章设置为未被逻辑删除
     *
     * @param aid 文章id
     */
    public void openArticle(Long aid) {
        log.info("正在恢复文章...");
        this.changeArticleStatus(aid, 0);
        log.info("恢复文章成功...");
    }

    /**
     * 批量开启文章
     *
     * @param articleIds 要开启的文章id集合
     */
    public void batchOpenArticles(List<Long> articleIds) {
        log.info("批量恢复文章开始...");
        for (Long aid : articleIds) {
            this.changeArticleStatus(aid, 0);
        }
        log.info("批量恢复文章完成...");
    }

    /**
     * 根据 aid 查找文章
     *
     * @param aid 文章id
     * @return 文章
     */
    public Article getArticleById(Long aid) {
        log.info("正在查找文章id为" + aid + "的文章...");
        return (Article) redisUtils.zRangeByScore(articleKey, aid, aid).iterator().next();
    }

    /**
     * 更新文章
     *
     * @param newArticle 新的文章数据
     */
    public void updateArticle(Article newArticle) {
        log.info("正在修改文章...");
        Long aid = newArticle.getAid();
        // 先根据 文章id 找到要修改的文章
        Article oldArticle = (Article) redisUtils.zRangeByScore(articleKey, aid, aid).iterator().next();
        // 将缺失的参数补齐
        newArticle.setCreatedTime(oldArticle.getCreatedTime());
        newArticle.setUpdatedTime(new Date());
        // 将修改前的文章删除
        redisUtils.zRemove(articleKey, oldArticle);
        // 将修改后的文章插入
        redisUtils.zAdd(articleKey, newArticle, aid);
        log.info("修改文章成功...");
    }

    /**
     * 根据 aid 删除一篇文章
     *
     * @param aid 文章id
     */
    public void deleteArticle(Long aid) {
        log.info("正在删除文章id为" + aid + "的文章...");
        redisUtils.zRemoveRangeByScore(articleKey, aid, aid);
        log.info("删除文章id为" + aid + "的文章成功...");
    }

    /**
     * 批量删除文章
     *
     * @param batchDelIds 文章id集合
     */
    public void batchDeleteArticle(List<Long> batchDelIds) {
        log.info("批量删除文章开始...");
        for (Long aid : batchDelIds) {
            this.deleteArticle(aid);
        }
        log.info("批量删除文章完成...");
    }

    /**
     * 根据 cid 批量删除文章
     *
     * @param cid 分类id
     */
    public void batchDeleteArticleByCid(Long cid) {
        log.info("批量删除文章开始...");
        List<Article> allArticles = this.getAllArticles();
        for (Article article : allArticles) {
            if (article.getCid().equals(cid)) {
                this.deleteArticle(article.getAid());
            }
        }
        log.info("批量删除文章完成...");
    }

    /**
     * 根据 cid 关闭文章
     *
     * @param cid 分类id
     */
    public void logicalDeleteArticleByCid(Long cid) {
        log.info("批量关闭文章开始...");
        List<Article> allArticles = this.getAllArticles();
        for (Article article : allArticles) {
            if (article.getCid().equals(cid)) {
                this.logicalDeleteArticle(article.getAid());
            }
        }
        log.info("批量关闭文章完成...");
    }

    /**
     * 根据 cid 关闭文章
     *
     * @param cid 分类id
     */
    public void openArticleByCid(Long cid) {
        log.info("批量恢复文章开始...");
        List<Article> allArticles = this.getAllArticles();
        for (Article article : allArticles) {
            if (article.getCid().equals(cid)) {
                this.openArticle(article.getAid());
            }
        }
        log.info("批量恢复文章完成...");
    }

    /**
     * 修改文章状态
     *
     * @param aid     文章id
     * @param deleted 1: 关闭该文章
     *                0: 开启该文章
     */
    private void changeArticleStatus(Long aid, Integer deleted) {
        // 找到要逻辑删除的文章
        Article article = this.getArticleById(aid);
        if (!article.getDeleted().equals(deleted)) {
            // 修改状态
            article.setDeleted(deleted);
            // 从缓存中将旧数据删除
            redisUtils.zRemoveRangeByScore(articleKey, aid, aid);
            // 将新数据存入缓存
            redisUtils.zAdd(articleKey, article, aid);
        }
    }

    /**
     * 倒序获取所有文章以及分类信息
     *
     * @return 所有文章
     */
    public List<Article> reverseGetAllOpenedArticlesWithCategory() {
        log.info("正在按 score 值从大到小获取所有文章缓存以及文章的分类信息...");
        List<Article> reverseArticleCache = new LinkedList<>();
        objSetToTList(redisUtils.zReverseRange(articleKey, 0, -1), reverseArticleCache);
        // 将被关闭了的文章删除
        reverseArticleCache.removeIf(article -> article.getDeleted() == 1);
        // 遍历每一篇文章, 将分类信息设置进去
        for (Article article : reverseArticleCache) {
            article.setCategory(categoryCache.getCategoryByCid(article.getCid()));
        }
        log.info("按 score 值从大到小获取所有文章缓存以及文章的分类信息成功...");
        return reverseArticleCache;
    }

    /**
     * 顺序获取所有文章以及分类信息
     *
     * @return 所有文章
     */
    public List<Article> getAllArticlesWithCategory() {
        log.info("正在按 score 值从小到大获取所有文章缓存以及文章的分类信息...");
        List<Article> articles = new LinkedList<>();
        objSetToTList(redisUtils.zRange(articleKey, 0, -1), articles);
        // 遍历每一篇文章, 将分类信息设置进去
        for (Article article : articles) {
            article.setCategory(categoryCache.getCategoryByCid(article.getCid()));
        }
        log.info("按 score 值从小到大获取所有文章缓存以及文章的分类信息成功...");
        return articles;
    }

    /**
     * 根据 cid 获取所有的文章
     *
     * @param cid 分类id
     * @return 该分类下的所有文章
     */
    public List<Article> getArticlesByCid(Long cid) {
        log.info("正在获取 cid = " + cid + " 的所有文章");
        List<Article> allArticles = this.getAllArticles();
        // 从所有文章中将 cid 不满足条件的删除
        allArticles.removeIf(article -> !cid.equals(article.getCid()));
        log.info("获取 cid = " + cid + " 的所有文章成功");
        return allArticles;
    }

    /**
     * 获取所有 开启中的文章 的 数目
     *
     * @return deleted = 0 的 文章数目
     */
    public Long getOpenedCount() {
        log.info("正在统计有多少篇文章开启中...");
        long count = 0;
        for (Object o : redisUtils.zRange(articleKey, 0, -1)) {
            if (((Article) o).getDeleted() == 0) {
                count++;
            }
        }
        log.info("一共有 " + count + " 篇文章开启中...");
        return count;
    }

    /**
     * 获取分页数据
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 分页数据
     */
    public List<Article> getOpenedArticlesByPageWithCategory(Integer page, Integer limit) {
        log.info("正在获取第 " + page + " 页的 " + limit + "条数据...");
        int start = (page - 1) * limit;
        int end = page * limit;
        List<Article> articleCache = this.reverseGetAllOpenedArticlesWithCategory();

        if (start > articleCache.size()) {
            return null;
        } else if (limit > articleCache.size()) {
            return articleCache;
        } else if (articleCache.size() >= end) {
            return articleCache.subList(start, end);
        } else {
            return articleCache.subList(start, articleCache.size());
        }
    }

    /**
     * 根据 aid 获取 文章数据及其分类信息
     *
     * @param aid 文章id
     * @return 带有分类信息的文章数据
     */
    public Article getArticleByIdWithCategory(Long aid) {
        log.info("正在获取 aid = " + aid + " 的文章数据及其分类信息...");
        // 通过 aid 找到文章
        Article article = this.getArticleById(aid);
        // 再根据该文章的 cid 找到 分类信息 填充
        article.setCategory(categoryCache.getCategoryByCid(article.getCid()));
        log.info("获取 aid = " + aid + " 的文章数据及其分类信息成功...");
        return article;
    }

    /**
     * 根据搜索条件分页获取文章数据
     *
     * @param searchDTO 查询参数
     * @return 筛选后再进行分页的文章数据
     */
    public List<Article> findArticlesByPageCondition(ArticlePageSearchDTO searchDTO) {
        log.info("正在获取第 " + searchDTO.getPage() + " 页的 " + searchDTO.getLimit() + "条数据...");

        List<Article> articles = this.dynamicSearch(searchDTO);

        int start = (searchDTO.getPage() - 1) * searchDTO.getLimit();
        int end = searchDTO.getPage() * searchDTO.getLimit();

        if (start > articles.size()) {
            return null;
        } else if (searchDTO.getLimit() > articles.size()) {
            return articles;
        } else if (articles.size() >= end) {
            return articles.subList(start, end);
        } else {
            return articles.subList(start, articles.size());
        }
    }

    /**
     * 统计 筛选后的文章总数
     *
     * @param searchDTO 搜索条件
     * @return 满足搜素条件的文章总数
     */
    public long countArticlesByPageConditionCount(ArticlePageSearchDTO searchDTO) {
        log.info("正在统计满足搜索条件的文章总数...");
        return this.dynamicSearch(searchDTO).size();
    }

    /**
     * 根据条件筛选文章数据
     *
     * @param searchDTO 筛选条件
     * @return 筛选后的所有文章数据
     */
    public List<Article> dynamicSearch(ArticlePageSearchDTO searchDTO) {
        log.info("正在筛选文章...");
        String author = searchDTO.getAuthor();
        String title = searchDTO.getTitle();
        Long cid = searchDTO.getCid();
        Integer deleted = searchDTO.getDeleted();

        List<Article> articles = this.getAllArticlesWithCategory();

        if (!isBlank(author)) {
            articles = articles.stream()
                    .filter(article -> article.getAuthor().contains(author))
                    .collect(Collectors.toList());
        }
        if (!isBlank(title)) {
            articles = articles.stream()
                    .filter(article -> article.getTitle().contains(title))
                    .collect(Collectors.toList());
        }
        if (null != cid) {
            articles = articles.stream()
                    .filter(article -> article.getCid().equals(cid))
                    .collect(Collectors.toList());
        }
        if (null != deleted) {
            articles = articles.stream()
                    .filter(article -> article.getDeleted().equals(deleted))
                    .collect(Collectors.toList());
        }
        log.info("筛选文章成功...");
        return articles;
    }

    /**
     * 根据当前 aid 查找上一篇文章( 即比当前文章发布时间早的第一篇文章 )
     *
     * @param aid 文章id
     * @return 上一篇文章
     */
    public Article findPrevArticleWithCategoryById(Long aid) {
        log.info("正在获取上一篇文章...");
        List<Article> articles = this.reverseGetAllOpenedArticlesWithCategory();
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getAid().equals(aid)) {
                return i == articles.size() - 1
                        ? null
                        : articles.get(i + 1);
            }
        }
        return null;
    }

    /**
     * 根据当前 aid 查找下一篇文章( 即比当前文章发布时间晚的第一篇文章 )
     *
     * @param aid 文章id
     * @return 下一篇文章
     */
    public Article findNextArticleWithCategoryById(Long aid) {
        log.info("正在获取下一篇文章...");
        List<Article> articles = this.reverseGetAllOpenedArticlesWithCategory();
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getAid().equals(aid)) {
                return i == 0
                        ? null
                        : articles.get(i - 1);
            }
        }
        return null;
    }

    /**
     * 随机获取一篇开启中的文章
     *
     * @return 随机一篇开启中的文章
     */
    public Article randomOneArticle() {
        log.info("正在随机获取一篇文章...");
        List<Article> articles = this.reverseGetAllOpenedArticlesWithCategory();
        // shuffle 打乱顺序
        Collections.shuffle(articles);
        log.info("随机获取一篇文章成功, aid = " + articles.get(0).getAid() + "...");
        // 将第一个返回
        return articles.get(0);
    }

    /**
     * 根据文章作者搜索文章
     *
     * @param searchCondition 搜索条件
     * @return 作者中包含搜索条件的文章列表
     */
    public List<Article> findAllOpenedArticlesByAuthor(String searchCondition) {
        log.info("正在搜索作者包含\" " + searchCondition +  " \"的文章...");
        List<Article> articles = this.reverseGetAllOpenedArticlesWithCategory()
                .stream()
                .filter(article -> article.getAuthor().toLowerCase().contains(searchCondition.toLowerCase()))
                .collect(Collectors.toList());
        log.info("搜索作者包含\" " + searchCondition +  " \"的文章共 " + articles.size() + " 篇...");
        return articles;
    }

    /**
     * 根据文章标题搜索文章
     *
     * @param searchCondition 搜索条件
     * @return 标题中包含搜索条件的文章列表
     */
    public List<Article> findAllOpenedArticlesByTitle(String searchCondition) {
        log.info("正在搜索标题包含\" " + searchCondition +  " \"的文章...");
        List<Article> articles = this.reverseGetAllOpenedArticlesWithCategory()
                .stream()
                .filter(article -> article.getTitle().toLowerCase().contains(searchCondition.toLowerCase()))
                .collect(Collectors.toList());
        log.info("搜索标题包含\" " + searchCondition +  " \"的文章共 " + articles.size() + " 篇...");
        return articles;
    }

    /**
     * 获取最新的一篇文章
     *
     * @return 最新的一篇文章
     */
    public Article getLatestOneArticle() {
        log.info("正在获取最新的一篇文章...");
        Article article =  (Article) redisUtils.zReverseRange(articleKey, 0, 0).iterator().next();
        // 填充分类信息
        article.setCategory(categoryCache.getCategoryByCid(article.getCid()));
        log.info("获取最新的一篇文章成功...");
        return article;
    }
}
