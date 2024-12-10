/*
 * Copyright (c) 2020 pig4cloud Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pig4cloud.pig.auth.endpoint;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.admin.api.entity.SysOauthClientDetails;
import com.pig4cloud.pig.admin.api.feign.RemoteClientDetailsService;
import com.pig4cloud.pig.admin.api.vo.TokenVo;
import com.pig4cloud.pig.auth.support.handler.PigAuthenticationFailureEventHandler;
import com.pig4cloud.pig.common.core.constant.CacheConstants;
import com.pig4cloud.pig.common.core.constant.CommonConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.core.util.RetOps;
import com.pig4cloud.pig.common.core.util.SpringContextHolder;
import com.pig4cloud.pig.common.security.annotation.Inner;
import com.pig4cloud.pig.common.security.util.OAuth2EndpointUtils;
import com.pig4cloud.pig.common.security.util.OAuth2ErrorCodesExpand;
import com.pig4cloud.pig.common.security.util.OAuthClientException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lengleng
 * @date 2019/2/1 删除token端点
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/token")
public class PigTokenEndpoint {

	private final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter = new OAuth2AccessTokenResponseHttpMessageConverter();

	private final AuthenticationFailureHandler authenticationFailureHandler = new PigAuthenticationFailureEventHandler();

	private final OAuth2AuthorizationService authorizationService;

	private final RemoteClientDetailsService clientDetailsService;

	private final RedisTemplate<String, Object> redisTemplate;

	private final CacheManager cacheManager;

	/**
	 * 认证页面
	 * @param modelAndView
	 * @param error 表单登录失败处理回调的错误信息
	 * @return ModelAndView
	 */
	// 显示登录页面，如果存在登录错误，将错误信息传递到视图中
	@GetMapping("/login")
	public ModelAndView require(ModelAndView modelAndView, @RequestParam(required = false) String error) {
		// 设置视图名称为 "ftl/login"
		modelAndView.setViewName("ftl/login");
		// 将错误信息添加到模型中，供视图渲染
		modelAndView.addObject("error", error);
		return modelAndView;
	}

	/**
	 * 用户在授权页面确认授权信息时的处理
	 *
	 * @param principal 当前用户的主体信息（通常是用户名）
	 * @param modelAndView ModelAndView 用于返回视图和数据
	 * @param clientId OAuth2 客户端的唯一标识
	 * @param scope OAuth2 请求的权限范围
	 * @param state OAuth2 状态参数，用于防止跨站请求伪造（CSRF）攻击
	 * @return 返回一个 ModelAndView，视图名为 "ftl/confirm"，包含授权信息
	 */
	@GetMapping("/confirm_access")
	public ModelAndView confirm(Principal principal, ModelAndView modelAndView,
			@RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
			@RequestParam(OAuth2ParameterNames.SCOPE) String scope,
			@RequestParam(OAuth2ParameterNames.STATE) String state) {
		// 根据 clientId 获取客户端详细信息
		SysOauthClientDetails clientDetails = RetOps.of(clientDetailsService.getClientDetailsById(clientId))
			.getData()
			.orElseThrow(() -> new OAuthClientException("clientId 不合法"));

		// 获取客户端的授权范围列表（比如，客户端可以请求的权限）
		Set<String> authorizedScopes = StringUtils.commaDelimitedListToSet(clientDetails.getScope());
		// 将客户端信息、授权范围、当前用户名等添加到模型中，供视图渲染
		modelAndView.addObject("clientId", clientId);
		modelAndView.addObject("state", state);
		modelAndView.addObject("scopeList", authorizedScopes);
		modelAndView.addObject("principalName", principal.getName());
		// 设置视图名称为 "ftl/confirm"，显示授权确认页面
		modelAndView.setViewName("ftl/confirm");
		return modelAndView;
	}

	/**
	 * 处理用户退出登录，并删除与该用户相关的 OAuth2 令牌
	 *
	 * @param authHeader HTTP 请求头中的 Authorization 字段，包含 Bearer 令牌
	 * @return 返回成功的响应，表示退出成功
	 */
	@DeleteMapping("/logout")
	public R<Boolean> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		// 如果 Authorization 头为空，直接返回成功（无令牌，不需要做任何操作）
		if (StrUtil.isBlank(authHeader)) {
			return R.ok();
		}

		// 提取出令牌值，去掉 Bearer 前缀
		String tokenValue = authHeader.replace(OAuth2AccessToken.TokenType.BEARER.getValue(), StrUtil.EMPTY).trim();
		// 调用 removeToken 方法删除该令牌
		return removeToken(tokenValue);
	}

	/**
	 * 校验传入的 OAuth2 令牌是否有效
	 *
	 * @param token OAuth2 令牌
	 * @param response HTTP 响应对象，用于设置响应状态码和输出
	 * @param request HTTP 请求对象，传递给认证失败处理器
	 * @throws Exception 如果令牌无效或找不到令牌，则抛出异常
	 */
	@SneakyThrows
	@GetMapping("/check_token")
	public void checkToken(String token, HttpServletResponse response, HttpServletRequest request) {
		// 使用 ServletServerHttpResponse 封装 response 对象
		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);

		// 如果令牌为空，返回 401 未授权状态，并触发认证失败处理器
		if (StrUtil.isBlank(token)) {
			httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
			this.authenticationFailureHandler.onAuthenticationFailure(request, response,
					new InvalidBearerTokenException(OAuth2ErrorCodesExpand.TOKEN_MISSING));
			return;
		}
		// 查找与令牌关联的 OAuth2 授权对象
		OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);

		// 如果找不到令牌或令牌无效，返回 401 状态并触发认证失败处理
		if (authorization == null || authorization.getAccessToken() == null) {
			this.authenticationFailureHandler.onAuthenticationFailure(request, response,
					new InvalidBearerTokenException(OAuth2ErrorCodesExpand.INVALID_BEARER_TOKEN));
			return;
		}

		// 获取令牌的声明（claims），并创建 OAuth2AccessTokenResponse 对象
		Map<String, Object> claims = authorization.getAccessToken().getClaims();
		OAuth2AccessTokenResponse sendAccessTokenResponse = OAuth2EndpointUtils.sendAccessTokenResponse(authorization,
				claims);
		// 将生成的访问令牌响应输出到 HTTP 响应中
		this.accessTokenHttpResponseConverter.write(sendAccessTokenResponse, MediaType.APPLICATION_JSON, httpResponse);
	}

	/**
	 * 删除指定的 OAuth2 令牌及其相关信息
	 *
	 * @param token 要删除的令牌
	 * @return 返回成功响应，表示令牌已删除
	 */
	@Inner
	@DeleteMapping("/remove/{token}")
	public R<Boolean> removeToken(@PathVariable("token") String token) {
		// 根据令牌查找 OAuth2 授权对象
		OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
		if (authorization == null) {
			return R.ok(); // 如果没有找到该令牌，直接返回成功
		}

		// 获取令牌中的 AccessToken
		OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
		if (accessToken == null || StrUtil.isBlank(accessToken.getToken().getTokenValue())) {
			return R.ok();  // 如果 AccessToken 无效，直接返回成功
		}
		// 清空用户信息（立即删除）
		cacheManager.getCache(CacheConstants.USER_DETAILS).evictIfPresent(authorization.getPrincipalName());
		// 清空access token
		authorizationService.remove(authorization);
		// 触发自定义的登出事件，用于记录日志或其他处理
		SpringContextHolder.publishEvent(new LogoutSuccessEvent(new PreAuthenticatedAuthenticationToken(
				authorization.getPrincipalName(), authorization.getRegisteredClientId())));
		return R.ok();
	}

	/**
	 * 查询所有的 OAuth2 令牌，并分页显示
	 *
	 * @param params 分页参数（当前页码和每页记录数）
	 * @return 返回分页后的令牌列表
	 */
	@Inner
	@PostMapping("/page")
	public R<Page> tokenList(@RequestBody Map<String, Object> params) {
		// 获取缓存中的 OAuth2 令牌数据
		String key = String.format("%s::*", CacheConstants.PROJECT_OAUTH_ACCESS);
		int current = MapUtil.getInt(params, CommonConstants.CURRENT);  // 当前页码
		int size = MapUtil.getInt(params, CommonConstants.SIZE); // 每页显示记录数
		// 获取所有匹配的缓存键（令牌的存储键）
		Set<String> keys = redisTemplate.keys(key);
		// 对缓存的键进行分页处理
		List<String> pages = keys.stream().skip((current - 1) * size).limit(size).collect(Collectors.toList());
		// 创建分页对象
		Page result = new Page(current, size);

		// 获取对应令牌的详细信息并转换为 TokenVo 对象列表
		List<TokenVo> tokenVoList = redisTemplate.opsForValue().multiGet(pages).stream().map(obj -> {
			OAuth2Authorization authorization = (OAuth2Authorization) obj;
			TokenVo tokenVo = new TokenVo();
			tokenVo.setClientId(authorization.getRegisteredClientId()); // 设置客户端 ID
			tokenVo.setId(authorization.getId()); // 设置令牌 ID
			tokenVo.setUsername(authorization.getPrincipalName()); // 设置用户名
			OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
			tokenVo.setAccessToken(accessToken.getToken().getTokenValue()); // 设置访问令牌值

			// 格式化令牌的到期时间和发放时间
			String expiresAt = TemporalAccessorUtil.format(accessToken.getToken().getExpiresAt(),
					DatePattern.NORM_DATETIME_PATTERN);
			tokenVo.setExpiresAt(expiresAt);

			String issuedAt = TemporalAccessorUtil.format(accessToken.getToken().getIssuedAt(),
					DatePattern.NORM_DATETIME_PATTERN);
			tokenVo.setIssuedAt(issuedAt);
			return tokenVo;
		}).collect(Collectors.toList());
		// 设置分页结果
		result.setRecords(tokenVoList);
		result.setTotal(keys.size()); // 设置令牌总数
		return R.ok(result);
	}

}
