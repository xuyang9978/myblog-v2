package run.xuyang.myblogv2.service.impl;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.dto.ArticlePageDTO;
import run.xuyang.myblogv2.dto.ArticlePageSearchDTO;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.mapper.ArticleMapper;
import run.xuyang.myblogv2.service.ArticleService;

import java.util.List;

/**
 * 文章 业务层接口实现类
 *
 * @author XuYang
 * @date 2020/7/17 22:14
 */
@Service("articleService")
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public List<Article> findAllArticlesWithCategory() {
        return articleMapper.findAllArticlesWithCategory();
    }

    @Override
    public List<Article> findAllArticlesWithoutCategory() {
        return articleMapper.findAllArticlesWithoutCategory();
    }

    @Override
    public int addArticle(Article article) {
        return articleMapper.insert(article);
    }

    @Override
    public Article getLatestArticle() {
        return articleMapper.getLatestArticle();
    }

    @Override
    public int persistCache2DB(List<Article> allArticles) {
        for (Article a : allArticles) {
            if (articleMapper.updateArticle(a) == 0) {
                return 0;
            }
        }
        return allArticles.size();
    }

    @Override
    public int deleteArticle(Long aid) {
        return articleMapper.deleteArticle(aid);
    }

    @Override
    public int batchDeleteArticle(List<Long> batchDelIds) {
        return articleMapper.batchDeleteArticle(batchDelIds);
    }

}
