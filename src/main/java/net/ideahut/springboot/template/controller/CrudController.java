package net.ideahut.springboot.template.controller;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import net.ideahut.springboot.annotation.Public;
import net.ideahut.springboot.crud.CrudAction;
import net.ideahut.springboot.crud.CrudHandler;
import net.ideahut.springboot.crud.CrudPermission;
import net.ideahut.springboot.crud.CrudResource;
import net.ideahut.springboot.crud.WebMvcCrudController;
import net.ideahut.springboot.object.Result;
import net.ideahut.springboot.task.TaskHandler;

/*
 * CrudPermission dan CrudResource bisa di level Handler ataupun di lever Controller.
 * Untuk di level Handler akan berlaku disetiap penggunaan CrudHandler
 * Untuk di level Controller hanya akan berlaku di setiap pemanggilan endpoint Crud
 */

//@Public(always = true)
@Public
@ComponentScan
@RestController
@RequestMapping("/crud")
class CrudController extends WebMvcCrudController {
	
	private final CrudHandler crudHandler;
	private final CrudResource crudResource;
	private final CrudPermission crudPermission;
	
	@Autowired
	CrudController(
		CrudHandler crudHandler,
		CrudResource crudResource,
		CrudPermission crudPermission
	) {
		this.crudHandler = crudHandler;
		this.crudResource = crudResource;
		this.crudPermission = crudPermission;
	}
	
	@Override
	protected CrudHandler crudHandler() {
		return crudHandler;
	}
	
	@Override
	protected CrudResource crudResource() {
		return crudResource;
	}
	
	@Override
	protected CrudPermission crudPermission() {
		return crudPermission;
	}
	
	@Override
	protected TaskHandler taskHandler() {
		return null;
	}
	
	/*
	 * CONSTANT
	 */
	@Override
	@GetMapping(value = "/constant")
	public Result constant() {
		return super.constant();
	}
	
	/*
	 * BULK LIST
	 */
	@Override
	@PostMapping(value = "/bulk/list")
	public List<Result> bulkList(
		HttpServletRequest httpRequest
	) {
		return super.bulkList(httpRequest);
	}
	
	/*
	 * BULK MAP
	 */
	@Override
	@PostMapping(value = "/bulk/map")
	public Map<String, Result> bulkMap(
		HttpServletRequest httpRequest
	) {
		return super.bulkMap(httpRequest);
	}
	
	/*
	 * ACTION
	 */
	@PostMapping(value = "/action/{action}")
	public Result action(
		@PathVariable("action") String action,
		HttpServletRequest httpRequest
	) {
		return super.body(CrudAction.of(action), httpRequest);
	}
	
	/*
	 * PARAMETER
	 */
	@RequestMapping(
		value = "/parameter/{action}", 
		method = { 
			RequestMethod.GET, 
			RequestMethod.POST, 
			RequestMethod.PUT, 
			RequestMethod.DELETE 
		}
	)
	public Result parameter(
		@PathVariable("action") String action,
		HttpServletRequest httpRequest
	) {
		return super.parameter(CrudAction.of(action), httpRequest);		
	}
	
	/*
	 * SINGLE
	 */
	@Override
	@GetMapping(value = "/rest")
	public Result single(
		HttpServletRequest httpRequest
	) {
		return super.single(httpRequest);
	}
	
	/*
	 * PAGE
	 */
	@Override
	@GetMapping(value = "/rest/page")
	public Result page(
		HttpServletRequest httpRequest		
	) {
		return super.page(httpRequest);
	}
	
	/*
	 * CREATE
	 */
	@Override
	@PostMapping(value = "/rest")
	public Result create(
		HttpServletRequest httpRequest
	) {
		return super.create(httpRequest);
	}
	
	/*
	 * UPDATE
	 */
	@Override
	@PutMapping(value = "/rest")
	public Result update(
		HttpServletRequest httpRequest
	) {
		return super.update(httpRequest);
	}
	
	/*
	 * DELETE 
	 */
	@Override
	@DeleteMapping(value = "/rest")
	public Result delete(
		HttpServletRequest httpRequest
	) {
		return super.delete(httpRequest);
	}
	
}
