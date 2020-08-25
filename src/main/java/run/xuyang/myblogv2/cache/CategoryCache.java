package run.xuyang.myblogv2.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.service.CategoryService;
import run.xuyang.myblogv2.util.RedisUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static run.xuyang.myblogv2.util.TypeConvertUtils.objSetToTList;

/**
 * 分类缓存
 *
 * @author XuYang
 * @date 2020/8/8 17:54
 */
@Component
@Slf4j
public class CategoryCache implements ApplicationRunner {

    private final CategoryService categoryService;

    private final RedisUtils redisUtils;

    // 为了解决循环依赖, 使用 @Autowired 完全交给 Spring 处理
    @Autowired
    private ArticleCache articleCache;

    public CategoryCache(CategoryService categoryService, RedisUtils redisUtils) {
        this.categoryService = categoryService;
        this.redisUtils = redisUtils;
    }

    private static final String categoryKey = "categoryCache";

    /**
     * 项目启动时将分类数据加载入缓存
     *
     * @param args 参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 从数据库中读取所有分类进行缓存
        log.info("正在缓存分类...");
        // 这里有个坑, key 里面数据量太大删除 key 可能会阻塞比较长的时间
        redisUtils.delete(categoryKey);
        List<Category> allArticlesWithoutCategory = categoryService.findAllCategoriesWithoutArticles();
        for (Category category : allArticlesWithoutCategory) {
            redisUtils.zAdd(categoryKey, category, category.getCid());
        }
        log.info("缓存分类完成...");
    }

    /**
     * 获取分类总数
     *
     * @return 分类总数
     */
    public long getCount() {
        log.info("正在统计分类数量...");
        return redisUtils.zSize(categoryKey);
    }

    /**
     * 返回所有分类
     *
     * @return 所有分类
     */
    public List<Category> getAllCategories() {
        log.info("正在获取所有的分类...");
        List<Category> categoryCache = new LinkedList<>();
        Set<Object> objects = redisUtils.zRange(categoryKey, 0, -1);
        objSetToTList(objects, categoryCache);
        log.info("获取所有的分类成功...");
        return categoryCache;
    }

    /**
     * 获取分页后的分类数据
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 该页分类数据
     */
    public List<Category> getCategoriesByPage(Integer page, Integer limit) {
        log.info("正在获取第 " + page + " 页的 " + limit + "条数据...");
        int start = (page - 1) * limit;
        int end = page * limit;
        List<Category> categoryCache = this.getAllCategories();

        if (start > categoryCache.size()) {
            return null;
        } else if (limit > categoryCache.size()) {
            return categoryCache;
        } else if (categoryCache.size() >= end) {
            return categoryCache.subList(start, end);
        } else {
            return categoryCache.subList(start, categoryCache.size());
        }
    }

    /**
     * 插入一条新的分类
     *
     * @param category 分类
     */
    public void insert(Category category) {
        log.info("正在向缓存中插入分类...");
        redisUtils.zAdd(categoryKey, category, category.getCid());
        log.info("向缓存中插入分类完成...");
    }

    /**
     * 修改分类
     *
     * @param newCategory 新的分类数据
     */
    public void updateCategory(Category newCategory) {
        log.info("正在修改分类...");
        Long cid = newCategory.getCid();
        Category oldCategory = (Category) redisUtils.zRangeByScore(categoryKey, cid, cid).iterator().next();
        newCategory.setCreatedTime(oldCategory.getCreatedTime());
        newCategory.setUpdatedTime(new Date());
        redisUtils.zRemove(categoryKey, oldCategory);
        redisUtils.zAdd(categoryKey, newCategory, cid);
        log.info("修改分类成功...");
    }

    /**
     * 删除一个分类
     *
     * @param cid 分类id
     */
    public void deleteCategory(Long cid) {
        log.info("正在删除 cid = " + cid + "的分类以及该分类下的所有文章...");
        redisUtils.zRemoveRangeByScore(categoryKey, cid, cid);
        // 将该分类下的所有文章全部删除
        articleCache.batchDeleteArticleByCid(cid);
        log.info("删除 cid = " + cid + "的分类以及该分类下的所有文章成功...");
    }

    /**
     * 批量删除分类
     *
     * @param batchDelIds 分类id集合
     */
    public void batchDeleteCategory(List<Long> batchDelIds) {
        log.info("批量删除分类以及分类下的所有文章开始...");
        for (Long cid : batchDelIds) {
            this.deleteCategory(cid);
            articleCache.batchDeleteArticleByCid(cid);
        }
        log.info("批量删除分类以及分类下的所有文章完成...");
    }

    /**
     * 根据 cid 关闭分类及该分类下的所有文章
     *
     * @param cid 分类id
     */
    public void logicDeleteCategory(Long cid) {
        log.info("正在逻辑删除 cid = " + cid + " 的分类一以及该分类下的所有文章...");
        this.changeCategoryStatus(cid, 1);
        articleCache.logicalDeleteArticleByCid(cid);
        log.info("逻辑删除 cid = " + cid + " 的分类一以及该分类下的所有文章成功...");
    }

    /**
     * 开启一个分类及该分类下的所有文章
     *
     * @param cid 分类id
     */
    public void openCategory(Long cid) {
        log.info("正在恢复 cid = " + cid + "的分类及该分类下的所有文章...");
        this.changeCategoryStatus(cid, 0);
        articleCache.openArticleByCid(cid);
        log.info("恢复 cid = " + cid + "的分类及该分类下的所有文章成功...");

    }

    /**
     * 根据 cid 获取一个分类
     *
     * @param cid 分类id
     * @return 分类
     */
    public Category getCategoryByCid(Long cid) {
        log.info("正在获取 cid = " + cid + " 的分类信息...");
        return (Category) redisUtils.zRangeByScore(categoryKey, cid, cid).iterator().next();
    }

    /**
     * 修改分类状态
     *
     * @param cid     分类id
     * @param deleted 1: 关闭该分类
     *                0: 开启该分类
     */
    private void changeCategoryStatus(Long cid, Integer deleted) {
        // 找到要逻辑删除的文章
        Category category = this.getCategoryByCid(cid);
        if (!category.getDeleted().equals(deleted)) {
            // 修改状态
            category.setDeleted(deleted);
            // 从缓存中将旧数据删除
            redisUtils.zRemoveRangeByScore(categoryKey, cid, cid);
            // 将新数据存入缓存
            redisUtils.zAdd(categoryKey, category, cid);
        }
    }

    /**
     * 获取所有开启中的的分类以及分类下的所有文章
     *
     * @return 所有开启中的分类以及分类下的所有文章
     */
    public List<Category> getAllOpenedCategoriesWithArticles() {
        log.info("正在获取所有开启中的的分类以及分类下的所有文章...");
        List<Category> categories = new LinkedList<>();
        objSetToTList(redisUtils.zRange(categoryKey, 0, -1), categories);
        // 将被关闭了的分类从列表中删除
        categories.removeIf(category -> category.getDeleted() == 1);
        for (Category category : categories) {
            category.setArticles(
                    // 把每个分类中的被关闭的文章删除从集合中删除
                    articleCache.getArticlesByCid(category.getCid())
                            .stream()
                            .filter(article -> article.getDeleted() == 0)
                            .collect(Collectors.toList())
            );
        }
        log.info("获取所有开启中的的分类以及分类下的所有文章成功..");
        return categories;
    }

    /**
     * 根据 cid 获取分类数据及该分类下的文章列表
     *
     * @param cid 分类id
     * @return 分类以及该分类下的文章列表
     */
    public Category getCategoryByCidWithArticles(Long cid) {
        log.info("正在获取 cid = " + cid + " 的分类数据及该分类下的文章列表...");
        Category category = this.getCategoryByCid(cid);
        category.setArticles(articleCache.getArticlesByCid(cid));
        log.info("正在获取 cid = " + cid + " 的分类数据及该分类下的文章列表成功...");
        return category;
    }

    public List<Category> findAllOpenedArticlesByCategory(String searchCondition) {
        log.info("正在获取分类名包含\" " + searchCondition + " \"的分类及包含的文章...");
        return this.getAllOpenedCategoriesWithArticles();
    }
}
