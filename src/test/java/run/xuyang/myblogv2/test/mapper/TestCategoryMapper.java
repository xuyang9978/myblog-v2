package run.xuyang.myblogv2.test.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.mapper.CategoryMapper;

import java.util.List;

/**
 * 测试 CategoryMapper 功能
 * @author XuYang
 * @date 2020/7/17 21:22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCategoryMapper {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 测试 {@link CategoryMapper#findAllCategoriesWithArticles()} 方法
     */
    @Test
    public void testFindAllCategories() {
        List<Category> allCategories = categoryMapper.findAllCategoriesWithArticles();
        allCategories.forEach(System.out::println);
    }
}
