package run.xuyang.myblogv2.test.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.service.ArticleService;
import run.xuyang.myblogv2.util.RedisUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author XuYang
 * @date 2020/8/1 11:09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestArticleCache {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ArticleService articleService;

    @Test
    public void testArticleList() {
        List<Article> articles = articleService.findAllArticlesWithCategory();
        for (Article article : articles) {
            redisUtils.zAdd("articleZSet", article, article.getAid());
        }
        Set<Object> articleZSet = redisUtils.zReverseRange("articleZSet", 0, -1);
        for (Object o : articleZSet) {
            System.out.println(((Article) o).getAid());
        }
    }
}
