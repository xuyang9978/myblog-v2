package run.xuyang.myblogv2.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

/**
 * 七牛云 上传图片 工具类
 *
 * @author XuYang
 * @date 2020/8/2 19:11
 */
@Component
@ConfigurationProperties(prefix = "qiniu")
@Getter
@Setter
public class QiNiuYunUtils {

    /**
     * AccessKey
     */
    private String accessKey;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 存储空间名
     */
    private String bucket;

    /**
     * 外链
     */
    private String path;


    /**
     * 将图片上传到七牛云
     */
    public String uploadQiniuYun(FileInputStream file) {
        System.out.println("accessKey=" + accessKey);
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        // 生成上传凭证, 然后准备上传
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(file, null, upToken, null, null);
                // 解析上传成功的结果
                DefaultPutRet putRet = new ObjectMapper().readValue(response.bodyString(), DefaultPutRet.class);
                // : 是样式分隔符, style 是样式名称( 全都在七牛云进行设置 )
                return path + "/" + putRet.key + ":" + "style";
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

