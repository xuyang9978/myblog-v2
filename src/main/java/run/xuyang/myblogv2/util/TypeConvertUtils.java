package run.xuyang.myblogv2.util;

import java.util.List;
import java.util.Set;

/**
 * 类型转换相关工具类
 *
 * @author XuYang
 * @date 2020/8/8 17:02
 */
public final class TypeConvertUtils {

    private TypeConvertUtils() { }

    /**
     * 将 Set<Object> 转换为 List<T>, 并将结果存在 List<T> 中
     *
     * @param objectSet Set<Object>
     * @param list      存放转换后的结果
     * @param <T>       泛型
     */
    @SuppressWarnings("unchecked")
    public static <T> void objSetToTList(Set<Object> objectSet, List<T> list) {
        for (Object o : objectSet) {
            list.add((T) o);
        }
    }
}
