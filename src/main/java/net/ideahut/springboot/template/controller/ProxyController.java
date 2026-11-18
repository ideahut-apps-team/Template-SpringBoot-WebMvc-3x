package net.ideahut.springboot.template.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import net.ideahut.springboot.annotation.Body;
import net.ideahut.springboot.annotation.Public;
import net.ideahut.springboot.api.ApiService;
import net.ideahut.springboot.api.ApiSource;
import net.ideahut.springboot.helper.WebMvcHelper;
import net.ideahut.springboot.rest.RestMethod;
import net.ideahut.springboot.rest.RestRequest;
import net.ideahut.springboot.rest.RestResponse;

/*
 * API untuk request ke ApiService yang lain
 */
@Public
@ComponentScan
@RestController
@RequestMapping("/proxy")
class ProxyController {
	
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();
	
	private final ApiService apiService;
	
	@Autowired
	ProxyController(
		ApiService apiService
	) {
		this.apiService = apiService;
	}
	
	/*
	 * Meminta token API consumer untuk melakukan request
	 * Token akan disimpan di SysParams, sysCode = "API_TOKEN", paramCode = <API_NAME>
	 */
	@PostMapping("/token")
	public String token(
		@RequestParam("apiName") String apiName
	) {
		ApiSource apiSource = apiService.getApiSource(apiName);
		return apiService.getApiSourceGate(apiSource).createApiToken(apiSource);
	}

	/*
	 * Proxy request ke ApiService lain
	 * - menggunakan ApiToken yang tersimpan di SysParam
	 * - semua http method diijinkan
	 * 
	 */
	@Body
	@RequestMapping(
		path = "/request/{apiName}/**", 
		method = { 
			RequestMethod.GET, 
			RequestMethod.POST, 
			RequestMethod.PUT, 
			RequestMethod.DELETE,
			RequestMethod.HEAD,
			RequestMethod.PATCH,
			RequestMethod.TRACE
		}
	)
	public ResponseEntity<byte[]> request(
		@PathVariable("apiName") String apiName,
		HttpServletRequest httpRequest
	) throws Exception {
		String fullPath = (String) httpRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	    String matchPattern = (String) httpRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	    String apiPath = pathMatcher.extractPathWithinPattern(matchPattern, fullPath);
	    RestRequest restRequest = new RestRequest()
		.setPath("/" + apiPath)
		.setMethod(RestMethod.valueOf(httpRequest.getMethod().toUpperCase()))
		.setQueryString(WebMvcHelper.getQueryString(httpRequest))
		.setHeaders(WebMvcHelper.getHeaders(httpRequest));
		if (!RestMethod.GET.equals(restRequest.getMethod())) {
			byte[] requestBody = WebMvcHelper.getBodyAsBytes(httpRequest);
			restRequest.setBody(requestBody);
		} else {
			restRequest.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
		}
		RestResponse restResponse = apiService.callEndpoint(apiName, restRequest).throwError();
		ResponseEntity<byte[]> responseEntity = ResponseEntity
		.status(restResponse.getStatus())
		.body(restResponse.getBodyAsByteArray());
		for (String restHeaderName : restResponse.getHeaderNames()) {
			List<String> restHeaderValues = restResponse.getHeaderValues(restHeaderName);
			responseEntity.getHeaders().addAll(restHeaderName, restHeaderValues);
		}
		return responseEntity;
	}
	
}
