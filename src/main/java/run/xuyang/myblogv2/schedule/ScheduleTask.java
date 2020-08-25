package run.xuyang.myblogv2.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.cache.ArticleCache;
import run.xuyang.myblogv2.cache.CategoryCache;
import run.xuyang.myblogv2.cache.LinkCache;
import run.xuyang.myblogv2.service.ArticleService;
import run.xuyang.myblogv2.service.CategoryService;
import run.xuyang.myblogv2.service.LinkService;

import java.time.LocalDate;

/**
 * 定时任务
 *
 * @author XuYang
 * @date 2020/8/9 12:42
 */
@Component
@Slf4j
@Transactional
public class ScheduleTask {

    private final ArticleCache articleCache;

    private final CategoryCache categoryCache;

    private final LinkCache linkCache;

    private final ArticleService articleService;

    private final CategoryService categoryService;

    private final LinkService linkService;

    public ScheduleTask(ArticleCache articleCache, CategoryCache categoryCache, LinkCache linkCache, ArticleService articleService, CategoryService categoryService, LinkService linkService) {
        this.articleCache = articleCache;
        this.categoryCache = categoryCache;
        this.linkCache = linkCache;
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.linkService = linkService;
    }


    /**
     * 每天 0点 将缓存中的 文章和分类 持久化到数据库中
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void persistCache2DB() {
        log.info("正在将文章缓存持久化到数据中...( " + LocalDate.now() + " )");
        if (articleService.persistCache2DB(articleCache.getAllArticles()) != 0) {
            log.info("持久化文章成功...");
        } else {
            log.info("持久化文章失败...");
        }
        log.info("正在将分类缓存持久化到数据中...( " + LocalDate.now() + " )");
        if (categoryService.persistCache2DB(categoryCache.getAllCategories()) != 0) {
            log.info("持久化分类成功...");
        } else {
            log.info("持久化分类失败...");
        }
        log.info("正在将友链缓存持久化到数据中...( " + LocalDate.now() + " )");
        if (linkService.persistCache2DB(linkCache.getAllLinks()) != 0) {
            log.info("持久化友链成功...");
        } else {
            log.info("持久化友链失败...");
        }
    }
}
