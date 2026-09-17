package net.ideahut.springboot.template.app;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.ControllerAdvice;

import net.ideahut.springboot.admin.AdminHandler;
import net.ideahut.springboot.advice.WebMvcAdvice;
import net.ideahut.springboot.helper.FrameworkHelper;
import net.ideahut.springboot.helper.ObjectHelper;
import net.ideahut.springboot.helper.StringHelper;
import net.ideahut.springboot.object.StringSet;

/*
 * - Untuk menghandle semua error yang terjadi di aplikasi
 */

@ControllerAdvice
class AppAdvice extends WebMvcAdvice {

	private final ApplicationContext applicationContext;
	private final AppProperties appProperties;
	private final AdminHandler adminHandler;
	
	@Autowired
	AppAdvice(
		ApplicationContext applicationContext,
		AppProperties appProperties,
		AdminHandler adminHandler
	) {
		this.applicationContext = applicationContext;
		this.appProperties = appProperties;
		this.adminHandler = adminHandler;
	}

	@Override
	protected boolean logAllError() {
		return !Boolean.FALSE.equals(appProperties.getLogAllError());
	}

	@Override
	protected Collection<String> exceptionSkipPaths() {
		StringSet skipPaths = new StringSet();
		skipPaths.add(adminHandler.getWebPath() + "/**");
		return skipPaths;
	}

	@Override
	protected Collection<String> bodyWriteSkipPaths() {
		String actuatorBasePath = FrameworkHelper.getActuatorBasePath(applicationContext);
		StringSet skipPaths = new StringSet();
		ObjectHelper.runIf(!StringHelper.isBlank(actuatorBasePath), () -> skipPaths.add(actuatorBasePath + "/**"));
		return skipPaths;
	}
	
}
