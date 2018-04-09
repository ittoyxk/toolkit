/** 
 * @author 王文超 
 * @time 2016年4月22日 上午9:36:24  
 * 类说明 
*/ 
package com.xk.mybatis;

import java.lang.annotation.*;

/** 
 * @author xiaokang
 * @Description 在dao里面加入注解，会使用批量mybatis批量插入形式
 * @time 2016年3月30日 上午9:32:49 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchAnnotation
{

}
