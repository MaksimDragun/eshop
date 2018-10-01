package org.dragberry.eshop.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dragberry.eshop.dal.entity.AuditRecord;
import org.dragberry.eshop.dal.repo.AuditRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This intercepter which logs customer's requests
 * 
 * @author Drahun Maksim
 *
 */
@Component
public class AuditLogFilter extends OncePerRequestFilter {

	private static final List<String> EXCLUDE_STATIC = Arrays.asList("/images", "/js", "/css", "/webfonts", "/favicon.ico");

	@Autowired
	private AuditRecordRepository auditRecordRepo;

	/**
	 * The default value is "false" so that the filter may log a "before" message at
	 * the start of request processing and an "after" message at the end from when
	 * the last asynchronously dispatched thread is exiting.
	 */
	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return false;
	}

	/**
	 * Forwards the request to the next filter in the chain and delegates down to
	 * the subclasses to perform the actual request logging both before and after
	 * the request is processed.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		boolean isFirstRequest = !isAsyncDispatch(request);
		HttpServletRequest requestToUse = request;

		if (isFirstRequest && !(request instanceof CustomHttpServletRequestWrapper)) {
			requestToUse = new CustomHttpServletRequestWrapper(request);
		}

		if (shouldLog(requestToUse) && isFirstRequest) {
			auditRecordRepo.save(createAuditRecord(requestToUse));
		}

		filterChain.doFilter(requestToUse, response);
	}

	/**
	 * Creates an AusitRecord from request
	 * 
	 * @return
	 */
	private AuditRecord createAuditRecord(HttpServletRequest request) {
		AuditRecord record = new AuditRecord();
		record.setAddress(request.getRemoteAddr());
		record.setEncoding(request.getCharacterEncoding());
		record.setLocale(request.getLocale().toString());
		record.setMethod(request.getMethod());
		record.setSessionId(request.getSession(false) != null ? request.getSession(false).getId() : null);
		record.setUri(request.getRequestURI());
		record.setQueryString(request.getQueryString());
		record.setBody(getMessagePayload(request));
		auditRecordRepo.save(record);
		return record;
	}

	/**
	 * Extracts the message payload portion of the message created by
	 */
	@Nullable
	protected String getMessagePayload(HttpServletRequest request) {
		try {
			return IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
		} catch (IOException ex) {
			return "[unknown]";
		}
	}

	/**
	 * Determine if the audit record should be logged. Static resources should not
	 * be processed
	 * 
	 * @param request
	 * @return
	 */
	protected boolean shouldLog(HttpServletRequest request) {
		return EXCLUDE_STATIC.stream().noneMatch(staticUri -> request.getRequestURI().startsWith(staticUri));
	}
	
	private class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

		private final byte[] body; 
		
		public CustomHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
			super(request);
			body = IOUtils.toByteArray(request.getReader(), request.getCharacterEncoding());
		}
		
		@Override
		public ServletInputStream getInputStream() throws IOException {
			final ByteArrayInputStream bais = new ByteArrayInputStream(body);
			return new ServletInputStream() {
				
				@Override
				public int read() throws IOException {
					return bais.read();
				}
				
				@Override
				public void setReadListener(ReadListener listener) {
				}
				
				@Override
				public boolean isReady() {
					return false;
				}
				
				@Override
				public boolean isFinished() {
					return false;
				}
			};
		}
		
	}

}
