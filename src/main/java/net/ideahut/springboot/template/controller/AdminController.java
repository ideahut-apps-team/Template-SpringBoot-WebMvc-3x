package net.ideahut.springboot.template.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.ideahut.springboot.admin.AdminHandler;
import net.ideahut.springboot.admin.WebMvcAdminController;
import net.ideahut.springboot.annotation.ApiExclude;
import net.ideahut.springboot.mapper.DataMapper;
import net.ideahut.springboot.security.WebMvcSecurity;

/*
 * API untuk Admin UI
 */
@ApiExclude
@ComponentScan
@RestController
@RequestMapping("/_/api")
class AdminController extends WebMvcAdminController {
	
	private final AdminHandler adminHandler;
	private final WebMvcSecurity webSecurity;
	
	@Autowired
	AdminController(
		DataMapper dataMapper,
		AdminHandler adminHandler,
		WebMvcSecurity webSecurity
	) {
		this.adminHandler = adminHandler;
		this.webSecurity = webSecurity;
	}
	
	@Override
	protected AdminHandler adminHandler() {
		return adminHandler;
	}
	
	@Override
	protected WebMvcSecurity webSecurity() {
		return webSecurity;
	}
	
}
