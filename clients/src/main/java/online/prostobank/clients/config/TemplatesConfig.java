package online.prostobank.clients.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collections;

/**
 *
 * @author yurij
 */
@Configuration
public class TemplatesConfig implements ApplicationContextAware, EnvironmentAware {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}

	@Override
	public void setEnvironment(Environment environment) {
	}

	@Bean @Primary
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        // Resolver for TEXT emails
        templateEngine.addTemplateResolver(textTemplateResolver());
        // Resolver for HTML emails (except the editable one)
		// and html to PDF
		templateEngine.addTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

	@Bean
	public ISpringTemplateEngine templateEngine() {
		SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
		springTemplateEngine.setEnableSpringELCompiler(true);
		springTemplateEngine.addTemplateResolver(textTemplateResolver());
		springTemplateEngine.addTemplateResolver(htmlTemplateResolver());
		return springTemplateEngine;
	}

	@Bean
	public ViewResolver viewResolver(ISpringTemplateEngine iSpringTemplateEngine) {
		ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
		thymeleafViewResolver.setTemplateEngine(iSpringTemplateEngine);
		thymeleafViewResolver.setCharacterEncoding("UTF-8");
		return thymeleafViewResolver;
	}

	private ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setResolvablePatterns(Collections.singleton("*"));
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".thyme");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
		templateResolver.setCheckExistence(true);
        return templateResolver;
    }

	private ITemplateResolver htmlTemplateResolver() {
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setOrder(2);
		templateResolver.setResolvablePatterns(Collections.singleton("*"));
		templateResolver.setPrefix("/templates/html/");
		templateResolver.setSuffix(".thyme");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(false);
		templateResolver.setCheckExistence(true);
		return templateResolver;
	}
}
