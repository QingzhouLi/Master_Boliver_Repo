package rpc;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class CORSFilter Cross-origin resource sharing
 * is a mechanism that allows a web page to make XMLHttpRequests to another
 * domain
 */
// Enable it for Servlet 3.x implementations
@WebFilter(asyncSupported = true, urlPatterns = { "/*" })
public class CORSFilter implements Filter {

	/**
	 * Default constructor.
	 */
	public CORSFilter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		System.out.println("CORSFilter HTTP Request: " + request.getMethod());

		// Authorize (allow) all domains to consume the content
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "http://boliver-frontend.s3-website-us-west-1.amazonaws.com");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods",
				"GET, OPTIONS, HEAD, PUT, POST");

		HttpServletResponse resp = (HttpServletResponse) servletResponse;

		// For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS
		// handshake
		if (request.getMethod().equals("OPTIONS")) {
			resp.setStatus(HttpServletResponse.SC_ACCEPTED);
			return;
		}

		// pass the request along the filter chain
		chain.doFilter(request, servletResponse);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}

/*
 * 
 * 跨来源资源共享（CORS）是一份浏览器技术的规范，提供了 Web 服务从不同网域传来沙盒脚本的方法， 以避开浏览器的同源策略，是 JSONP
 * 模式的现代版。与 JSONP 不同，CORS 除了 GET 要求方法以外也支持其他的 HTTP 要求。 用 CORS 可以让网页设计师用一般的
 * XMLHttpRequest，这种方式的错误处理比 JSONP 要来的好。 另一方面，JSONP 可以在不支持 CORS
 * 的老旧浏览器上运作。现代的浏览器都支持 CORS。
 * 
 * Response Headers
 * 
 * Access-Control-Allow-Origin : specifies the authorized domains to make
 * cross-domain request. Use “*” as value if there is no restrictions.
 * Access-Control-Allow-Credentials : specifies if cross-domain requests can
 * have authorization credentials or not. Access-Control-Expose-Headers :
 * indicates which headers are safe to expose. Access-Control-Max-Age :
 * indicates how long the results of a preflight request can be cached.
 * Access-Control-Allow-Methods : indicates the methods allowed when accessing
 * the resource. Access-Control-Allow-Headers : indicates which header field
 * names can be used during the actual request. Request Headers
 * 
 * Origin : indicates where the cross-origin actual request or preflight request
 * originates from. Access-Control-Request-Method : used when issuing a
 * preflight request to let the server know what HTTP method will be used in
 * actual request. Access-Control-Request-Headers : used when issuing a
 * preflight request to let the server know what HTTP headers will be used in
 * actual request.
 * 
 */
