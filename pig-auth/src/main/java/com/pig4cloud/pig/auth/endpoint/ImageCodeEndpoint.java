package com.pig4cloud.pig.auth.endpoint;

import cn.hutool.core.lang.Validator;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import io.springboot.captcha.ArithmeticCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 验证码相关的接口
 *
 * @author lengleng
 * @date 2022/6/27
 */
@RestController
@RequestMapping("/code")
@RequiredArgsConstructor
public class ImageCodeEndpoint {

	private static final Integer DEFAULT_IMAGE_WIDTH = 100;

	private static final Integer DEFAULT_IMAGE_HEIGHT = 40;

	private final RedisTemplate redisTemplate;

	/**
	 * 创建图形验证码
	 */
	@SneakyThrows
	@GetMapping("/image")
	public void image(String randomStr, HttpServletResponse response) {
		// 创建一个算术验证码对象，设置图片的宽度和高度
		ArithmeticCaptcha captcha = new ArithmeticCaptcha(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		// 校验传入的 randomStr 是否为一个有效的手机号码，如果是，则不生成验证码直接返回
		if (Validator.isMobile(randomStr)) {
			return; // 如果是手机号码，直接返回，不生成验证码
		}

		// 获取算术验证码的文本内容，即计算题的答案（例如：12 + 8 = ?）
		String result = captcha.text();
		// 将验证码的答案存储到 Redis 中，以便后续验证，缓存的键为 'DEFAULT_CODE_KEY + randomStr'
		// 缓存有效期由 'SecurityConstants.CODE_TIME' 指定，单位为秒
		redisTemplate.opsForValue()
			.set(CacheConstants.DEFAULT_CODE_KEY + randomStr, result, SecurityConstants.CODE_TIME, TimeUnit.SECONDS);
		// 将生成的验证码图片流写入到响应输出流中，直接返回给客户端
		captcha.out(response.getOutputStream());
	}

}
