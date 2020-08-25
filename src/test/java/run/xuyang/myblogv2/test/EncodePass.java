package run.xuyang.myblogv2.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author XuYang
 * @date 2020/8/11 13:20
 */
public class EncodePass {

    public static void main(String[] args) {
        String encode = new BCryptPasswordEncoder().encode("123");
        System.out.println(encode);
        System.out.println(new BCryptPasswordEncoder().matches(
                "123",
                encode));
    }
}
