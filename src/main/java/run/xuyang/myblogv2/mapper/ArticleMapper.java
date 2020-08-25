package run.xuyang.myblogv2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import run.xuyang.myblogv2.dto.ArticlePageDTO;
import run.xuyang.myblogv2.dto.ArticlePageSearchDTO;
import run.xuyang.myblogv2.entity.Article;

import java.util.Date;
import java.util.List;

/**
 * 文章 的持久层接口
 *
 * @author XuYang
 * @date 2020/7/17 18:02
 */
@Repository
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 查询 所有文章, 包含分类信息
     *
     * @return 所有文章
     */
    @Results(id = "articleMapWithCategory", value = {
            @Result(id = true, column = "aid", property = "aid", javaType = Long.class),
            @Result(column = "title", property = "title", javaType = String.class),
            @Result(column = "content", property = "content", javaType = String.class),
            @Result(column = "html_content", property = "htmlContent", javaType = String.class),
            @Result(column = "author", property = "author", javaType = String.class),
            @Result(column = "created_time", property = "createdTime", javaType = Date.class),
            @Result(column = "updated_time", property = "updatedTime", javaType = Date.class),
            @Result(column = "cid", property = "cid", javaType = Long.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class),
            @Result(column = "cid", property = "category", one = @One(select = "run.xuyang.myblogv2.mapper.CategoryMapper.findCategoryByCid", fetchType = FetchType.EAGER))
    })
    @Select(" select aid, title, content, html_content, author, created_time, updated_time, deleted, cid " +
            " from article ")
    List<Article> findAllArticlesWithCategory();

    /**
     * 根据 cid 查询所有的文章, 不包含分类信息
     *
     * @return 指定 cid 下的所有文章
     */
    @ResultMap("articleMapWithoutCategory")
    @Select(" select aid, title, content, html_content, author, created_time, updated_time, deleted, cid " +
            " from article " +
            " where cid = #{cid} ")
    List<Article> findArticlesByCid(@Param("cid") Long cid);

    /**
     * 查询 所有文章, 不包含分类信息
     *
     * @return 所有文章
     */
    @Results(id = "articleMapWithoutCategory", value = {
            @Result(id = true, column = "aid", property = "aid", javaType = Long.class),
            @Result(column = "title", property = "title", javaType = String.class),
            @Result(column = "content", property = "content", javaType = String.class),
            @Result(column = "html_content", property = "htmlContent", javaType = String.class),
            @Result(column = "author", property = "author", javaType = String.class),
            @Result(column = "created_time", property = "createdTime", javaType = Date.class),
            @Result(column = "updated_time", property = "updatedTime", javaType = Date.class),
            @Result(column = "cid", property = "cid", javaType = Long.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class)
    })
    @Select(" select aid, title, content, html_content, author, created_time, updated_time, deleted, cid " +
            " from article ")
    List<Article> findAllArticlesWithoutCategory();

    /**
     * 获取 最新发表的文章, 包含分类信息
     *
     * @return 最新发表的文章
     */
    @ResultMap("articleMapWithCategory")
    @Select(" select aid, title, content, html_content, author, created_time, updated_time, deleted, cid " +
            " from article " +
            " order by aid desc " +
            " limit 1 ")
    Article getLatestArticle();

    /**
     * 根据 aid 查询文章
     *
     * @param aid 文章id
     * @return 文章
     */
    @ResultMap("articleMapWithoutCategory")
    @Select(" select aid, title, content, html_content, author, created_time, updated_time, deleted, cid " +
            " from article " +
            " where aid = #{aid}")
    Article findArticleByAid(@Param(value = "aid") Long aid);

    /**
     * 更新文章
     *
     * @param a 文章
     * @return 影响的记录数
     */
    @Update(" update article " +
            " set title=#{a.title}, content=#{a.content}, html_content=#{a.htmlContent}, " +
            "     updated_time=#{a.updatedTime}, cid=#{a.cid}, author=#{a.author}, deleted=#{a.deleted} " +
            " where aid=#{a.aid} ")
    int updateArticle(@Param(value = "a") Article a);

    /**
     * 根据 aid 删除文章
     * @param aid 文章id
     * @return 影响的记录数
     */
    @Delete(" delete from article where aid=#{aid} ")
    int deleteArticle(@Param(value = "aid") Long aid);

    /**
     * 根据 aid 批量删除文章
     *
     * @param batchDelIds 文章id集合
     * @return 影响的记录数
     */
    int batchDeleteArticle(List<Long> batchDelIds);
}
