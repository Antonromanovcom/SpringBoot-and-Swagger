package online.prostobank.clients.utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimpleCORSFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse) response;
		res.setHeader("Access-Control-Allow-Headers", "x-csrf-token");
		res.setHeader("Access-Control-Expose-Headers", "x-csrf-token");
		res.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
		chain.doFilter(request, res);
	}
}
