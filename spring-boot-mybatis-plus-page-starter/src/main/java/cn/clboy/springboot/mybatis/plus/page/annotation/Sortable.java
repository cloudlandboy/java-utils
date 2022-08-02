package cn.clboy.springboot.mybatis.plus.page.annotation;

import java.lang.annotation.*;

/**
 * 注解标注字段表示可参与排序
 *
 * @author sunYunLa
 * @date 2022/8/2 上午9:57
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Sortable {

    /**
     * @return 排序优先级, 值越小，优先级越高
     */
    int sortPriority() default 0;

    /**
     * @return 查询时对应列名
     */
    String column() default "";
}
