package cn.clboy.springboot.mybatis.plus.page.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * 可排序字段信息
 *
 * @author sunYunLa
 * @date 2022/8/2 上午10:05
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class SortableFieldInfo {

    /**
     * 原始属性
     */
    private final Field field;

    /**
     * 字段名
     */
    private final String column;

    /**
     * 属性名
     */
    private final String property;

    /**
     * 排序优先级
     */
    private final int sortPriority;
}
