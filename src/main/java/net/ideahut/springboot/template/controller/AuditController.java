package net.ideahut.springboot.template.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import net.ideahut.springboot.audit.AuditHandler;
import net.ideahut.springboot.audit.AuditRequest;
import net.ideahut.springboot.helper.ErrorHelper;
import net.ideahut.springboot.helper.ObjectHelper;
import net.ideahut.springboot.helper.StringHelper;
import net.ideahut.springboot.helper.WebMvcHelper;
import net.ideahut.springboot.object.Page;
import net.ideahut.springboot.object.Result;
import net.ideahut.springboot.template.Application;

/*
 * API untuk melihat data audit
 */
@ComponentScan
@RestController
@RequestMapping("/audit")
class AuditController {
	
	private final AuditHandler auditHandler;
	
	@Autowired
	AuditController(
		AuditHandler auditHandler
	) {
		this.auditHandler = auditHandler;
	}
	
	
	@PostMapping(value = "/list")
	public Result list(HttpServletRequest httpRequest) throws Exception {
		byte[] data = WebMvcHelper.getBodyAsBytes(httpRequest);
		AuditRequest auditRequest = auditHandler.getRequest(data);
		String entity = ObjectHelper.useOrDefault(auditRequest.getEntity(), "").trim();
		ObjectHelper.callIf(
			!StringHelper.isEmpty(entity) && auditRequest.getClassOfEntity() == null, 
			() -> {
				Class<?> classOfEntity = ObjectHelper.useOrDefault(
					ObjectHelper.safeClassOf(entity), 
					() -> ObjectHelper.safeClassOf(Application.Package.APPLICATION + ".entity." + entity)
				);
				ErrorHelper.throwNull(classOfEntity, () -> "Entity not found: " + entity);
				return auditRequest.setClassOfEntity(classOfEntity);
			}
		);			
		Page page = auditHandler.getList(auditRequest);
		return Result.success(page);
	}
	
	
	@GetMapping(value = "/bytes")
	public Result bytes(
		@RequestParam(name = "manager", required = false) String manager,
		@RequestParam(name = "id") String id
	) {
		byte[] bytes = auditHandler.getBytes(manager, id);
		return Result.success(bytes);
	}
	
	
}
