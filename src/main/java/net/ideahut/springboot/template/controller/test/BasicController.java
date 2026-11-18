package net.ideahut.springboot.template.controller.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
		return StringHelper.toBytes("BYTES-" + System.nanoTime());
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
	public void outstream(
		HttpServletResponse response
	) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() {
			private byte[] bytes = StringHelper.toBytes("Haloooo-" + System.nanoTime());
			@Override
			public synchronized void writeTo(OutputStream outputStream) throws IOException {
				outputStream.write(bytes);
			}
		};
		try {
			byteArrayOutputStream.writeTo(response.getOutputStream());
		} catch (IOException e) {
			throw ErrorHelper.exception(e);
		}
	}
	
	// contoh download streaming, liat WebConfig bagian configureAsyncSupport
	// proses output stream secara asinkron menggunakan taskExecutor
	@GetMapping("/download")
	public StreamingResponseBody download(
		HttpServletResponse response
	) {
		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream outputStream) throws IOException {
				try {
					response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + System.nanoTime() + ".txt\"");
					response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
					for (int i = 0; i < 10; i++) {
						
						// simulasi proses write yang lambat
						TimeUnit.SECONDS.sleep(1);
						
						outputStream.write(StringHelper.toBytes("DATA-" + i + "\n"));
						outputStream.flush();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};
	}
	
	// contoh streaming baca request dan kirim response
	@PostMapping("/streaming")
	public StreamingResponseBody streaming(
		@RequestParam(name = "buffer", required = false) Integer buffer,
		@RequestParam(name = "delay", required = false) Integer delay,
		HttpServletRequest request
	) {
		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream outputStream) throws IOException {
				InputStream inputStream = request.getInputStream();
				try {
					Integer sleep = delay != null && delay > 0 ? delay : 100;
					Integer bufferSize = buffer != null && buffer > 0 ? buffer : 16;
					byte[] bytesBuffer = new byte[bufferSize];
					long totalBytes = 0;
		            int bytesRead;
		            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
		            	
		            	// simulasi proses write yang lambat
						TimeUnit.MILLISECONDS.sleep(sleep);
		            	
		                outputStream.write(bytesBuffer, 0, bytesRead);
		                outputStream.flush();
		                totalBytes += bytesRead;
		            }
		            byte[] endStreaming = StringHelper.toBytes("\n\nTotal Bytes: " + totalBytes);
		            outputStream.write(endStreaming, 0, endStreaming.length);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};
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
