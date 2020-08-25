package run.xuyang.myblogv2.test.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.mapper.ArticleMapper;

import java.util.List;

/**
 * 测试 ArticleMapper.xml 功能
 *
 * @author XuYang
 * @date 2020/7/17 21:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestArticleMapper {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 测试 #{@link ArticleMapper#findAllArticles()} 方法
     */
    @Test
    public void testFindAllArticles() {
        List<Article> allArticles = articleMapper.findAllArticlesWithCategory();
        allArticles.forEach(System.out::println);
    }

    /**
     * 测试 删除 逻辑删除一条记录
     */
    @Test
    public void testLogicDelete() {
        int res = articleMapper.deleteById(1L);
        System.out.println(res);
    }

}
