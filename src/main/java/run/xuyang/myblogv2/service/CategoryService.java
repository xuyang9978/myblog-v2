package run.xuyang.myblogv2.service;

import run.xuyang.myblogv2.entity.Category;

import java.util.Arrays;
import java.util.List;


/**
 * 分类 业务层接口
 *
 * @author XuYang
 * @date 2020/7/17 22:13
 */
public interface CategoryService {

    List<Category> findAllCategoriesWithoutArticles();

    int addCategory(Category category);

    Category findCategoryByName(String categoryName);

    int persistCache2DB(List<Category> allCategories);

    int deleteCategory(Long cid);
}
