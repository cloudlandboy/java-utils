package cn.clboy.springboot.mybatis.plus.page;

import cn.clboy.springboot.mybatis.plus.page.resolver.MybatisPlusPageArgumentResolver;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 分页参数解析器自动配置
 *
 * @author sunYunLa
 * @date 2022/8/2 下午3:44
 * @since 1.0.0
 */
@Configuration
@ConditionalOnWebApplication
public class MybatisPlusPageAutoConfiguration implements WebMvcConfigurer {


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MybatisPlusPageArgumentResolver());
    }
}
