package run.xuyang.myblogv2.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.entity.Link;
import run.xuyang.myblogv2.service.LinkService;
import run.xuyang.myblogv2.util.RedisUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static run.xuyang.myblogv2.util.TypeConvertUtils.objSetToTList;

/**
 * 友情链接缓存
 *
 * @author XuYang
 * @date 2020/8/13 20:00
 */
@Component
@Slf4j
public class LinkCache implements ApplicationRunner {

    private final LinkService linkService;

    private final RedisUtils redisUtils;

    public LinkCache(LinkService linkService, RedisUtils redisUtils) {
        this.linkService = linkService;
        this.redisUtils = redisUtils;
    }

    private static final String linkKey = "linkCache";

    /**
     * 项目启动时将友情链接数据加载入缓存
     *
     * @param args 参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 从数据库中读取所有友情链接进行缓存
        log.info("正在缓存友链...");
        // 这里有个坑, key 里面数据量太大删除 key 可能会阻塞比较长的时间
        redisUtils.delete(linkKey);
        List<Link> links = linkService.findAllLinks();
        for (Link link : links) {
            redisUtils.zAdd(linkKey, link, link.getLid());
        }
        log.info("缓存友链完成...");
    }

    /**
     * 获取分页后的友链数据
     *
     * @param page  第几页
     * @param limit 每页多少条数据
     * @return 该页友链数据
     */
    public List<Link> getLinksByPage(Integer page, Integer limit) {
        log.info("正在获取第 " + page + " 页的 " + limit + "条数据...");
        int start = (page - 1) * limit;
        int end = page * limit;
        List<Link> linkCache = this.getAllLinks();

        if (start > linkCache.size()) {
            return null;
        } else if (limit > linkCache.size()) {
            return linkCache;
        } else if (linkCache.size() >= end) {
            return linkCache.subList(start, end);
        } else {
            return linkCache.subList(start, linkCache.size());
        }
    }

    /**
     * 返回所有友链
     *
     * @return 所有友链
     */
    public List<Link> getAllLinks() {
        log.info("正在获取所有的友链...");
        List<Link> linkCache = new LinkedList<>();
        Set<Object> objects = redisUtils.zRange(linkKey, 0, -1);
        objSetToTList(objects, linkCache);
        log.info("获取所有的友链成功...");
        return linkCache;
    }

    /**
     * 获取友链总数
     *
     * @return 友链总数
     */
    public long getCount() {
        log.info("正在统计友链数量...");
        return redisUtils.zSize(linkKey);
    }

    /**
     * 插入一条新的友链
     *
     * @param link 友链
     */
    public void insert(Link link) {
        log.info("正在向缓存中插入友链...");
        redisUtils.zAdd(linkKey, link, link.getLid());
        log.info("向缓存中插入友链完成...");
    }

    /**
     * 批量删除友链
     *
     * @param batchDelIds 友链id集合
     */
    public void batchDeleteLink(List<Long> batchDelIds) {
        log.info("批量删除友链开始...");
        for (Long lid : batchDelIds) {
            this.deleteLink(lid);
        }
        log.info("批量删除文章完成...");
    }

    /**
     * 根据 lid 删除一个友链
     *
     * @param lid 友链id
     */
    public void deleteLink(Long lid) {
        log.info("正在删除友链id为" + lid + "的友链...");
        redisUtils.zRemoveRangeByScore(linkKey, lid, lid);
        log.info("删除友链id为" + lid + "的友链成功...");
    }

    /**
     * 根据 lid 查找友链
     *
     * @param lid 友链id
     * @return 友链
     */
    public Link getLinkById(Long lid) {
        log.info("正在查找友链id为" + lid + "的友链...");
        return (Link) redisUtils.zRangeByScore(linkKey, lid, lid).iterator().next();
    }

    /**
     * 修改友链
     *
     * @param newLink 新的友链数据
     */
    public void updateLink(Link newLink) {
        log.info("正在修改友链...");
        Long lid = newLink.getLid();
        Link oldLink = (Link) redisUtils.zRangeByScore(linkKey, lid, lid).iterator().next();
        redisUtils.zRemove(linkKey, oldLink);
        redisUtils.zAdd(linkKey, newLink, lid);
        log.info("修改友链成功...");
    }

    /**
     * 逻辑删除一条友链
     *
     * @param lid 友链id
     */
    public void logicalDeleteLink(Long lid) {
        log.info("正在逻辑删除友链...");
        this.changeLinkStatus(lid, 1);
        log.info("逻辑删除友链成功...");
    }

    /**
     * 恢复一条友链
     *
     * @param lid 友链id
     */
    public void openLink(Long lid) {
        log.info("正在恢复友链...");
        this.changeLinkStatus(lid, 0);
        log.info("恢复友链成功...");
    }

    /**
     * 修改友链状态
     *
     * @param lid     友链id
     * @param deleted 1: 关闭该文章
     *                0: 开启该文章
     */
    private void changeLinkStatus(Long lid, Integer deleted) {
        // 找到要逻辑删除的友链
        Link link = this.getLinkById(lid);
        if (!link.getDeleted().equals(deleted)) {
            // 修改状态
            link.setDeleted(deleted);
            // 从缓存中将旧数据删除
            redisUtils.zRemoveRangeByScore(linkKey, lid, lid);
            // 将新数据存入缓存
            redisUtils.zAdd(linkKey, link, lid);
        }
    }
}
