package cn.clboy.springboot.mybatis.plus.page.metadata;

import cn.clboy.springboot.mybatis.plus.page.annotation.Sortable;
import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分页辅助工具
 *
 * @author sunYunLa
 * @date 2022/8/2 上午10:19
 * @since 1.0.0
 */
public class PageInfoHelper {

    /**
     * 敏感词
     */
    private final static String[] KEYWORDS = {"master", "truncate", "insert", "select", "delete", "update", "declare",
            "alter", "drop", "sleep", "extractvalue", "concat"};

    /**
     * 可排序字段缓存
     */
    private static final Map<Class<?>, Map<String, SortableFieldInfo>> SORTABLE_FIELD_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据class信息获取可排序字段
     *
     * @param clazz class
     * @return 所有可排序字段映射
     * @author sunYunLa
     * @date 2022/8/2 上午10:42
     * @since 1.0.0
     */
    public static Map<String, SortableFieldInfo> getSortableFieldInfo(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive() || SimpleTypeRegistry.isSimpleType(clazz) || clazz.isInterface()) {
            return Collections.emptyMap();
        }
        return SORTABLE_FIELD_INFO_CACHE.computeIfAbsent(clazz, cz -> ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz)).stream()
                .filter(field -> field.getAnnotation(Sortable.class) != null)
                .map(field -> {
                    Sortable sortable = field.getAnnotation(Sortable.class);
                    String column = StringUtils.isNotBlank(sortable.column()) ? sortable.column() : StringUtils.camelToUnderline(field.getName());
                    return new SortableFieldInfo(field, column, field.getName(), sortable.sortPriority());
                }).flatMap(info -> {
                    Map.Entry<String, SortableFieldInfo> propertyEntry = new AbstractMap.SimpleEntry<>(info.getProperty(), info);
                    return info.getColumn().equals(info.getProperty()) ?
                            Stream.of(propertyEntry) :
                            Stream.of(propertyEntry, new AbstractMap.SimpleEntry<>(info.getColumn(), info));
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

}
