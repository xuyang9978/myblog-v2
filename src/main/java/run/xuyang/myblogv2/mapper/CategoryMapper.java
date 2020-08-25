package run.xuyang.myblogv2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;
import run.xuyang.myblogv2.entity.Category;

import java.util.Date;
import java.util.List;

/**
 * 分类 的持久层接口
 *
 * @author XuYang
 * @date 2020/7/17 18:03
 */
@Repository
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询所有的分类(包括已经被逻辑删除了的), 包含文章信息
     *
     * @return 所有的分类
     */
    @Results(id = "categoryMapWithArticles", value = {
            @Result(id = true, column = "cid", property = "cid", javaType = Long.class),
            @Result(column = "category_name", property = "categoryName", javaType = String.class),
            @Result(column = "created_time", property = "createdTime", javaType = Date.class),
            @Result(column = "updated_time", property = "updatedTime", javaType = Date.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class),
            @Result(column = "cid", property = "articles", many = @Many(select = "run.xuyang.myblogv2.mapper.ArticleMapper.xml.findArticlesByCid", fetchType = FetchType.LAZY))
    })
    @Select(" select cid, category_name, created_time, updated_time, deleted " +
            " from category ")
    List<Category> findAllCategoriesWithArticles();

    /**
     * 查询所有的分类(包括已经被逻辑删除了的), 不包含文章信息
     *
     * @return 所有的分类
     */
    @Results(id = "categoryMapWithoutArticles", value = {
            @Result(id = true, column = "cid", property = "cid", javaType = Long.class),
            @Result(column = "category_name", property = "categoryName", javaType = String.class),
            @Result(column = "created_time", property = "createdTime", javaType = Date.class),
            @Result(column = "updated_time", property = "updatedTime", javaType = Date.class),
            @Result(column = "deleted", property = "deleted", javaType = Integer.class)
    })
    @Select(" select cid, category_name, created_time, updated_time, deleted " +
            " from category ")
    List<Category> findAllCategoriesWithoutArticles();

    /**
     * 根据 分类名 查询一个分类, 不包含文章信息
     *
     * @param categoryName 分类名
     * @return 分类
     */
    @ResultMap("categoryMapWithoutArticles")
    @Select(" select cid, category_name, created_time, updated_time, deleted " +
            " from category " +
            " where category_name = #{categoryName} ")
    Category findCategoryByName(@Param(value = "categoryName") String categoryName);

    /**
     * 根据 cid 查询分类
     *
     * @param cid 分类id
     * @return 分类
     */
    @ResultMap("categoryMapWithoutArticles")
    @Select(" select cid, category_name, created_time, updated_time, deleted " +
            " from category " +
            " where cid = #{cid} ")
    Category findCategoryByCid(@Param(value = "cid") Long cid);

    /**
     * 更新分类
     *
     * @param c 分类
     * @return 影响的记录数
     */
    @Update(" update category " +
            " set category_name=#{c.categoryName}, updated_time=#{c.updatedTime}, deleted=#{c.deleted} " +
            " where cid=#{c.cid} ")
    int updateCategory(@Param(value = "c") Category c);

    /**
     * 根据 cid 删除分类
     *
     * @param cid 分类id
     * @return 影响的记录数
     */
    @Delete(" delete from category where cid=#{cid} ")
    int deleteCategory(@Param(value = "cid") Long cid);

}
