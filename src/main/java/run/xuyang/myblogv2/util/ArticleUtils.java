package run.xuyang.myblogv2.util;

import run.xuyang.myblogv2.entity.Article;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.hutool.core.util.StrUtil.isBlank;

/**
 * @author XuYang
 * @date 2020/7/26 10:32
 */
public final class ArticleUtils {

    private ArticleUtils() {
    }


    /**
     * 校验一篇文章必要的参数是否为空
     *
     * @param article 文章
     * @return 是否是一篇完整的文章
     */
    public static boolean notIsCompleteArticle(Article article) {
        return (isBlank(article.getAuthor())
                || isBlank(article.getTitle())
                || article.getCid() == null
                || article.getCid() <= 0);
    }

    /**
     * 清除所有的 html 标签
     *
     * @param htmlStr 带有 html 标签的字符串
     * @return 清除 html 标签后的字符串
     */
    public static String removeTag(String htmlStr) {
        // script
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        // style
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        // HTML tag
        String regEx_html = "<[^>]+>";
        // other characters
        String regEx_space = "\\s+|\t|\r|\n";

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll("");
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll("");

        return htmlStr;
    }

    public static String subString(String str) {
        str = removeTag(str);
        // 代表一句话结束的符号
        // 句号。
        // 问号？
        // 感叹号！
        int periodIndex = str.indexOf('。');
        int questionIndex = str.indexOf('？');
        int exclamationIndex = str.indexOf('！');
        int[] indexArr = {periodIndex, questionIndex, exclamationIndex};
        int index = minIndex(indexArr);
        return str.substring(0, index + 1);
    }

    /**
     * 获取index数组中 最小的 index, 保证 > 0
     *
     * @param indexArr index数组
     * @return 最小的index
     */
    private static int minIndex(int[] indexArr) {
        int index = indexArr[0];
        for (int i : indexArr) {
            if (index > i && i > 0) {
                index = i;
            }
        }
        return index;
    }

}
