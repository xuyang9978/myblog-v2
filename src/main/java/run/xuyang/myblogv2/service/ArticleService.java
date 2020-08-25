package run.xuyang.myblogv2.service;


import run.xuyang.myblogv2.dto.ArticlePageDTO;
import run.xuyang.myblogv2.dto.ArticlePageSearchDTO;
import run.xuyang.myblogv2.entity.Article;

import java.util.Arrays;
import java.util.List;

/**
 * 文章 业务层接口
 *
 * @author XuYang
 * @date 2020/7/17 22:13
 */
public interface ArticleService {

    List<Article>  findAllArticlesWithCategory();

    List<Article> findAllArticlesWithoutCategory();

    int addArticle(Article article);

    Article getLatestArticle();

    int persistCache2DB(List<Article> allArticles);

    int deleteArticle(Long aid);

    int batchDeleteArticle(List<Long> batchDelIds);
}
