package com.pig4cloud.pig.common.feign.annotation;

import java.lang.annotation.*;

/**
 * 服务无token调用声明注解
 * <p>
 * 只有发起方没有 token 时候才需要添加此注解， @NoToken + @Inner
 * <p>
 */
//@NoToken 注解的应用范围：只能用于方法上
@Target(ElementType.METHOD)
//@NoToken 注解在运行时仍然保留，因此可以通过反射机制被读取
@Retention(RetentionPolicy.RUNTIME)
//当使用 javadoc 生成 API 文档时，如果某个方法被 @NoToken 注解，那么这个注解会出现在该方法的文档中
@Documented
// 由于它是一个注解，因此使用 @interface 关键字进行声明，而不是普通的 interface
public @interface NoToken {

}
