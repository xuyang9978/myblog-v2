package run.xuyang.myblogv2.handler;

import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import run.xuyang.myblogv2.response.ResultResponse;

import java.util.Map;

/**
 * 全局异常处理(处理空参异常)
 *
 * @author XuYang
 * @date 2020/7/30 18:54
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BindException.class)
    public Map<String, Object> violationException(BindException exception) {
        // 不带任何参数访问接口,会抛出 BindException
        // 因此，我们只需捕获这个异常，并返回我们设置的 message 即可
        String message = exception.getAllErrors().get(0).getDefaultMessage();
        return ResultResponse.getFailResponse(400, message);
    }
}
