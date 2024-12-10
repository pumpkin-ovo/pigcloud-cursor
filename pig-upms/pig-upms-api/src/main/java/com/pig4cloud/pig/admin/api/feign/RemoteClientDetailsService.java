/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.pig4cloud.pig.admin.api.feign;

import com.pig4cloud.pig.admin.api.entity.SysOauthClientDetails;
import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.feign.annotation.NoToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lengleng
 * @date 2020/12/05
 */
// contextId 属性用于指定这个 Feign 客户端的上下文 ID（或称为 bean 名称）
// 	value 属性用于指定这个 Feign 客户端要调用的微服务的名称。这个名称通常是在微服务架构中用于服务发现和注册的名称
@FeignClient(contextId = "remoteClientDetailsService", value = ServiceNameConstants.UPMS_SERVICE)
public interface RemoteClientDetailsService {

	/**
	 * 通过clientId 查询客户端信息 (未登录，需要无token 内部调用)
	 * @param clientId 用户名
	 * @return R
	 */
	@NoToken
	@GetMapping("/client/getClientDetailsById/{clientId}")
	R<SysOauthClientDetails> getClientDetailsById(@PathVariable("clientId") String clientId);

}
