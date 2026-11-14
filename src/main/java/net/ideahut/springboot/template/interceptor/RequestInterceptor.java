package net.ideahut.springboot.template.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import net.ideahut.springboot.admin.AdminHandler;
import net.ideahut.springboot.api.ApiAccess;
import net.ideahut.springboot.api.ApiService;
import net.ideahut.springboot.api.WebMvcApiService;
import net.ideahut.springboot.function.TriFunction;
import net.ideahut.springboot.interceptor.DefaultWebMvcInterceptor;
import net.ideahut.springboot.security.WebMvcSecurity;
import net.ideahut.springboot.template.Application;
import net.ideahut.springboot.template.app.AppConstant;
import net.ideahut.springboot.template.app.AppProperties;

@Component
@ComponentScan
class RequestInterceptor extends DefaultWebMvcInterceptor {
	
	// set false, agar ApiService tidak melakukan pengecekan (development)
	private static final boolean CHECK_BY_API_SERVICE = true;
	
	// set true, jika tidak ingin mengecek ApiRole / ApiSource request (development)
	private static final boolean ALLOW_ALL_REQUEST = false;
	
	// allow all request function
	private static final TriFunction<ApiService, ApiAccess, HandlerMethod, Boolean> ALLOW_ALL_FUNCTION = 
	(service, apiAccess, handlerMethod) -> true;
	
	private final AppProperties appProperties;
	private final AdminHandler adminHandler;
	private final WebMvcSecurity adminSecurity;
	private final WebMvcApiService apiService;
	private final ApiAccess publicApiAccess;
	
	@Autowired
	RequestInterceptor(
		AppProperties appProperties,
		AdminHandler adminHandler,
		@Qualifier(AppConstant.Bean.Security.ADMIN)
		WebMvcSecurity adminSecurity,
		WebMvcApiService apiService
	) {
		this.appProperties = appProperties;
		this.adminHandler = adminHandler;
		this.adminSecurity = adminSecurity;
		this.apiService = apiService;
		this.publicApiAccess = ApiAccess.unmodifiable(new ApiAccess().setApiRole("PUBLIC"));
	}

	@Override
	protected boolean isServiceAvailable() {
		return Application.isReady();
	}
	
	@Override
	protected String publicBaseUrl() {
		return appProperties.getPublicBaseUrl();
	}
	
	@Override
	protected ApiAccess publicApiAccess() {
		return publicApiAccess;
	}
	
	@Override
	protected WebMvcApiService apiService() {
		return CHECK_BY_API_SERVICE ? apiService : null;
	}

	@Override
	protected AdminHandler adminHandler() {
		return adminHandler;
	}

	@Override
	protected WebMvcSecurity adminSecurity() {
		return adminSecurity;
	}

	@Override
	protected TriFunction<ApiService, ApiAccess, HandlerMethod, Boolean> allowFunction() {
		return ALLOW_ALL_REQUEST ? ALLOW_ALL_FUNCTION : super.allowFunction();
	}	
	
}
