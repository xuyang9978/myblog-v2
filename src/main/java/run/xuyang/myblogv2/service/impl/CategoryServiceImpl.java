package run.xuyang.myblogv2.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.xuyang.myblogv2.entity.Article;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.mapper.CategoryMapper;
import run.xuyang.myblogv2.service.CategoryService;

import java.util.List;


/**
 * 分类 业务层接口实现类
 *
 * @author XuYang
 * @date 2020/7/17 22:15
 */
@Service("categoryService")
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> findAllCategoriesWithoutArticles() {
        return categoryMapper.findAllCategoriesWithoutArticles();
    }

    @Override
    public int addCategory(Category category) {
        return categoryMapper.insert(category);
    }

    @Override
    public Category findCategoryByName(String categoryName) {
        return categoryMapper.findCategoryByName(categoryName);
    }

    @Override
    public int persistCache2DB(List<Category> allCategories) {
        for (Category c : allCategories) {
            if (categoryMapper.updateCategory(c) == 0) {
                return 0;
            }
        }
        return allCategories.size();
    }

    @Override
    public int deleteCategory(Long cid) {
        return categoryMapper.deleteCategory(cid);
    }

}
