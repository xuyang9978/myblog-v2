package run.xuyang.myblogv2.util;

import run.xuyang.myblogv2.entity.Category;
import run.xuyang.myblogv2.entity.Link;

import static cn.hutool.core.util.StrUtil.isBlank;

/**
 * @author XuYang
 * @date 2020/7/28 13:01
 */
public final class LinkUtils {

    private LinkUtils() {
    }

    /**
     * 校验一个友链对象必要的参数是否为空
     *
     * @param link 友链
     * @return 是否是一个完整的友链对象
     */
    public static boolean notIsCompleteLink(Link link) {
        return (isBlank(link.getLinkName())
                || isBlank(link.getLinkName())
                || isBlank(link.getLinkUrl()));
    }
}
