package online.prostobank.clients.interceptors;

import online.prostobank.clients.services.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;

@ControllerAdvice
public class CustomRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

	private final LoggingService loggingService;

	private final HttpServletRequest httpServletRequest;

	@Autowired
	public CustomRequestBodyAdviceAdapter(LoggingService loggingService, HttpServletRequest httpServletRequest) {
		this.loggingService = loggingService;
		this.httpServletRequest = httpServletRequest;
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

		loggingService.logRequest(httpServletRequest, body);

		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}
}
