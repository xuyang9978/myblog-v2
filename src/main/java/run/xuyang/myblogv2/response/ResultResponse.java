package run.xuyang.myblogv2.response;


import java.util.HashMap;
import java.util.Map;

/**
 * 后台给前台的响应数据
 *
 * @author XuYang
 * @date 2020/7/22 21:45
 */
public final class ResultResponse{

    private ResultResponse() {}

    private final static Map<String, Object> res = new HashMap<>();

    private static Map<String, Object> getResponse(Integer code, String msg) {
        res.put("code", code);
        res.put("msg", msg);
        return res;
    }

    public static Map<String, Object> getSuccessResponse(Integer code, String msg) {
        return getResponse(code, msg);
    }

    public static Map<String, Object> getFailResponse(Integer code, String msg) {
        return getResponse(code, msg);
    }


}
