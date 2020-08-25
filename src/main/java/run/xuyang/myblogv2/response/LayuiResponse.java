package run.xuyang.myblogv2.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * layui 数据表格的响应数据
 *
 * @author XuYang
 * @date 2020/7/19 14:01
 */
public final class LayuiResponse {

    private LayuiResponse() {
    }

    private final static Map<String, Object> res = new HashMap<>();

    private static Map<String, Object> getResponse(Integer code, String msg, Long count, List<?> data) {
        res.put("code", code);
        res.put("msg", msg);
        res.put("count", count);
        res.put("data", data);
        return res;
    }

    public static Map<String, Object> getSuccessResponse(Integer code, String msg, Long count, List<?> data) {
        return getResponse(code, msg, count, data);
    }

    public static Map<String, Object> getFailResponse(Integer code, String msg, Long count, List<?> data) {
        return getResponse(code, msg, count, data);
    }
}
