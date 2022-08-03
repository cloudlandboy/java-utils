package cn.clboy.springboot.mybatis.plus.page.resolver;

import cn.clboy.springboot.mybatis.plus.page.metadata.PageInfoHelper;
import cn.clboy.springboot.mybatis.plus.page.metadata.SortableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mp分页参数处理
 *
 * @author sunYunLa
 * @date 2022/8/2 下午2:30
 * @since 1.0.0
 */
public class MybatisPlusPageArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 当前页参数名
     */
    private static final String CURRENT_PAGE_PARAMETER_NAME = "current";

    /**
     * 总条数参数名
     */
    private static final String SIZE_PARAMETER_NAME = "size";

    /**
     * 升序字段参数名
     */
    private static final String ASCENDING_PARAMETER_NAME = "ascending";

    /**
     * 降序字段参数名
     */
    private static final String DESCENDING_PARAMETER_NAME = "descending";

    /**
     * 允许最大size
     */
    private static final long ALLOW_MAX_PAGE_SIZE = 1000L;

    /**
     * 判断参数是不是Page类型
     *
     * @param parameter 参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Page.class);
    }

    /**
     * 只支持查询GET请求,POST需要解析请求体
     *
     * @param parameter     入参集合
     * @param mavContainer  model 和 view
     * @param webRequest    web相关
     * @param binderFactory 入参解析
     * @return 检查后新的page对象
     * <p>
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String current = request.getParameter(CURRENT_PAGE_PARAMETER_NAME);
        String size = request.getParameter(SIZE_PARAMETER_NAME);

        Page page = new Page();
        if (StringUtils.hasText(current)) {
            page.setCurrent(Long.parseLong(current));
            if (page.getCurrent() <= 1) {
                page.setCurrent(1);
            }
        }

        if (StringUtils.hasText(size)) {
            page.setSize(Long.parseLong(size));
            Assert.isTrue(page.getSize() <= ALLOW_MAX_PAGE_SIZE, "page size max value allowed is " + ALLOW_MAX_PAGE_SIZE);
        }
        //解析排序
        page.setOrders(parseOrder(request, parameter));
        return page;
    }

    /**
     * 解析排序
     *
     * @param request   请求
     * @param parameter 参数信息
     * @return OrderItem
     * @author sunYunLa
     * @date 2022/8/2 上午10:49
     * @since 1.0.0
     */
    private List<OrderItem> parseOrder(HttpServletRequest request, MethodParameter parameter) {
        //从请求中获取升序和降序两个参数
        String[] ascending = request.getParameterValues(ASCENDING_PARAMETER_NAME);
        String[] descending = request.getParameterValues(DESCENDING_PARAMETER_NAME);
        if (ascending == null && descending == null) {
            return Collections.emptyList();
        }
        // 获取page泛型的类型
        Class<?> clazz = ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        //获取泛型类中的可排序字段
        Map<String, SortableFieldInfo> sortableFieldInfo = PageInfoHelper.getSortableFieldInfo(clazz);
        if (CollectionUtils.isEmpty(sortableFieldInfo)) {
            return Collections.emptyList();
        }
        //对两个排序参数处理。1.逗号拆分，2.过滤掉非可排序字段，3.去重，4.转换为OrderItem
        List<OrderItem> orderItemList = new ArrayList<>();
        Optional.ofNullable(ascending).ifPresent(s -> orderItemList.addAll(Arrays.stream(s)
                .flatMap(s1 -> StringUtils.commaDelimitedListToSet(s1).stream())
                .filter(sql -> sortSqlFilter(sql, sortableFieldInfo))
                .distinct()
                .map(sc -> OrderItem.asc(sortableFieldInfo.get(sc).getColumn()))
                .collect(Collectors.toList())));
        Optional.ofNullable(descending).ifPresent(s -> orderItemList.addAll(Arrays.stream(s)
                .flatMap(s1 -> StringUtils.commaDelimitedListToSet(s1).stream())
                .filter(sql -> sortSqlFilter(sql, sortableFieldInfo))
                .distinct()
                .map(sc -> OrderItem.desc(sortableFieldInfo.get(sc).getColumn()))
                .collect(Collectors.toList())));
        //进行优先级排序
        orderItemList.sort(Comparator.comparingInt(o -> sortableFieldInfo.get(o.getColumn()).getSortPriority()));
        return orderItemList;
    }

    /**
     * 排序字段过滤
     *
     * @param sql            前端传来的值
     * @param sortableFields 可排序值
     * @return boolean
     * @author sunYunLa
     * @date 2022/8/2 上午10:58
     * @since 1.0.0
     */
    private boolean sortSqlFilter(String sql, Map<String, SortableFieldInfo> sortableFields) {
        return StringUtils.hasText(sql) && sortableFields.containsKey(sql);
    }

}
