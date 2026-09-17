package net.ideahut.springboot.template.controller.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.ideahut.springboot.annotation.Public;
import net.ideahut.springboot.helper.ErrorHelper;
import net.ideahut.springboot.helper.StringHelper;
import net.ideahut.springboot.helper.ThreadHelper;
import net.ideahut.springboot.helper.WebMvcHelper;
import net.ideahut.springboot.object.Message;
import net.ideahut.springboot.object.Result;

/*
 * Contoh API untuk fungsi dasar http
 */
@Slf4j
@Public
@ComponentScan
@RestController
@RequestMapping("/test/basic")
class BasicController {

	@GetMapping("/exception")
	public void exception() {
		throw ErrorHelper.exception(() -> StringHelper.format("ERROR-{}", System.nanoTime()));
	}
	
	@GetMapping("/virtualThread")
	public Result virtualThread() {
		Thread thread = Thread.currentThread();
		boolean isVt = ThreadHelper.isThreadVirtual(thread);
		return Result.success(isVt).setInfo("thread", thread.getName());
	}
	
	@GetMapping("/bytes")
	public byte[] bytes() {
		return ("BYTES-" + System.nanoTime()).getBytes();
	}
	
	@GetMapping("/string")
	public String string() {
		return "STRING-" + System.nanoTime();
	}
	
	@GetMapping("/responseEntity")
	public ResponseEntity<String> responseEntity() {
		return ResponseEntity.ok()
		.header("Test-Strre", "string")
		.body("STRRE-" + System.nanoTime());
	}

	@GetMapping("/send")
	public void send(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		//WebMvcHelper.sendResponse(request, response, null, false, "SEND-" + System.nanoTime()); //-
		WebMvcHelper.sendResponse(request, response, "SEND-" + System.nanoTime()); //-
		//WebMvcHelper.sendResponse(request, response, null, false, System.nanoTime()); //-
		//WebMvcHelper.sendResponse(request, response, System.nanoTime()); //-
		//WebMvcHelper.sendResponse(request, response, new Exception("ERROR-SEND-" + System.nanoTime())); //-
		
		/**
		String hval = System.nanoTime() + "";
		response.setHeader("xxx1", hval);
		response.setHeader("xxx2", hval);
		ResponseEntity<Message> re = ResponseEntity.ok()
		.header("xxx2", "KEREN", "LAGI")
		.header("yyyy", "NONE")
		.body(Message.of("YYY", "VALUE"));
		WebMvcHelper.sendResponse(request, response, re);
		*/
	}
	
	@GetMapping("/result")
	public Result result() {
		return Result.success("RESULT-" + System.nanoTime());
	}
	
	@GetMapping("/message")
	public Message message() {
		return Message.of("MSG", "MESSAGE-{}", System.nanoTime());
	}
	
	@GetMapping("/outstream")
	public void outstream(HttpServletResponse response) {
		ByteArrayOutputStream out = new ByteArrayOutputStream() {
			private byte[] bytes = ("Haloooo-" + System.nanoTime()).getBytes();
			@Override
			public synchronized void writeTo(OutputStream out) throws IOException {
				out.write(bytes);
			}
		};
		try {
			out.writeTo(response.getOutputStream());
		} catch (IOException e) {
			throw ErrorHelper.exception(e);
		}
	}
	
	@PostMapping(value = "/multipart")
	public Result multipart(
		@RequestParam(name = "name") String name,
		@RequestParam(name = "file", required = false) MultipartFile file
	) throws Exception {
		Result result = Result.success()
		.setInfo("name", name);
		if (file != null) {
			result
			.setInfo("length", file.getBytes().length)
			.setInfo("filename", file.getOriginalFilename());
		}
		return result;
	}
	
	@GetMapping("/logger")
	public void logger() {
		Throwable throwable = new Exception(StringHelper.format("EXCEPTION: {}", System.nanoTime() + ""));
		log.debug("{}", message(() -> "DEBUG"), throwable);
		log.trace("{}", message(() -> "TRACE"), throwable);
		log.info("{}", message(() -> "INFO-" + System.nanoTime()), throwable);
		log.warn("{}", message(() -> "WARN"), throwable);
		log.error("{}", message(() -> "ERROR"), throwable);
	}
	
	private Object message(Supplier<CharSequence> message) {
		return new Object() {
			@Override
			public String toString() {
				CharSequence charSequence = message != null ? message.get() : null;
				return charSequence != null ? charSequence.toString() : "";
			}
		};
	}
	
}
