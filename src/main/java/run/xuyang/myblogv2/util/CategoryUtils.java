package run.xuyang.myblogv2.util;

import run.xuyang.myblogv2.entity.Category;

import static cn.hutool.core.util.StrUtil.isBlank;

/**
 * @author XuYang
 * @date 2020/7/28 13:01
 */
public final class CategoryUtils {

    private CategoryUtils() {}

    /**
     * 校验一个分类对象必要的参数是否为空
     *
     * @param category 分类
     * @return 是否是一个完整的分类对象
     */
    public static boolean notIsCompleteCategory(Category category) {
        return (isBlank(category.getCategoryName())
                || category.getCid() == null
                || category.getCid() < 0);
    }
}
