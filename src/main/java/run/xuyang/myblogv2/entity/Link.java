package run.xuyang.myblogv2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 友情链接实体类
 *
 * @author XuYang
 * @date 2020/8/13 19:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 链接id
     */
    private Long lid;

    /**
     * 链接名字
     */
    private String linkName;

    /**
     * 链接地址
     */
    private String linkUrl;

    /**
     * 逻辑删除, 0 表示开启链接, 1 表示关闭连接
     */
    private Integer deleted;
}
