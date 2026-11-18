package net.ideahut.springboot.template.controller.test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import net.ideahut.springboot.annotation.Body;
import net.ideahut.springboot.annotation.Public;
import net.ideahut.springboot.helper.ErrorHelper;
import net.ideahut.springboot.helper.FrameworkHelper;
import net.ideahut.springboot.helper.StringHelper;
import net.ideahut.springboot.report.ReportHandler;
import net.ideahut.springboot.report.ReportInput;
import net.ideahut.springboot.report.ReportType;
import net.ideahut.springboot.template.app.AppProperties;
import net.ideahut.springboot.template.object.ReportData;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;

/*
 * Contoh penggunaan ReportHandler
 */
@ComponentScan
@RestController
@RequestMapping("/test/report")
class ReportController implements InitializingBean {
	
	private final AppProperties appProperties;
	private final ReportHandler reportHandler;
	
	private byte[] template;
	private byte[] imageHeader;
	private byte[] imageDetail;
	
	
	@Autowired
	ReportController(
		AppProperties appProperties,
		ReportHandler reportHandler
	) {
		this.appProperties = appProperties;
		this.reportHandler = reportHandler;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String path = appProperties.getReportPath();
		ErrorHelper.throwBlank(path, () -> "AppProperties.reportPath is required");
		path = FrameworkHelper.replacePath(path);
		path = StringHelper.removeEnd(path, "/");
		template = FrameworkHelper.toByteArray(FrameworkHelper.getResource(path + "/sample.jasper"));
		imageHeader = FrameworkHelper.toByteArray(FrameworkHelper.getResource(path + "/tree1.png"));
		imageDetail = FrameworkHelper.toByteArray(FrameworkHelper.getResource(path + "/tree2.png"));
	}
	
	// contoh respon resource, memuat seluruh data ke memori
	@Body // tambahkan @Body agar respon tidak diubah ke standar framework
	@Public
	@GetMapping("/resource")
	public ResponseEntity<byte[]> resource(
	    @RequestParam("name") String name
	) {
	    ReportType type = getType(name);
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    export(type, outStream);
	    byte[] bytes = outStream.toByteArray();
	    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(type.getContentType()))
        .body(bytes);
	}
	
	// contoh respons stream, tidak memuat seluruh data ke memory
	@Public
	@GetMapping("/stream")
	public ResponseEntity<StreamingResponseBody> stream(
		@RequestParam("name") String name
	) {
		ReportType type = getType(name);
		StreamingResponseBody body = outStream -> export(type, outStream);
		return ResponseEntity.ok()
		.contentType(MediaType.valueOf(type.getContentType()))
		.body(body);
	}
	
	private ReportType getType(String name) {
		try {
			return ReportType.valueOf(name.trim().toUpperCase());
		} catch (Exception e) {
			return ReportType.PDF;
		}
	}
	
	private void export(ReportType type, OutputStream outputStream) {
		// agar report tidak disimpan di memory
		JRSwapFile swapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), 4096, 100);
		JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(0, swapFile, true);
		try (BufferedOutputStream bufferedOut = new BufferedOutputStream(outputStream, 8192)) {
			JasperReport report = (JasperReport) JRLoader.loadObject(new ByteArrayInputStream(template));
			List<ReportData> datasource = new ArrayList<>();
			for (long i = 0; i < 100; i++) {
				ReportData data = new ReportData();
				data.setDescription("Deskripsi - " + System.nanoTime());
				data.setName("Name - " + System.nanoTime());
				data.setNumber(i);
				data.setUuid(UUID.randomUUID().toString());
				datasource.add(data);
			}
			ReportInput input = new ReportInput()
			.setType(type)
			.setReport(report)
			.setDatasource(datasource)
			.setParameter(JRParameter.REPORT_VIRTUALIZER, virtualizer)
			.setParameter("MAIN_TITLE", "Contoh Report")
			.setParameter("SUB_TITLE", type.name())
			.setParameter("IMAGE_HEADER", new ByteArrayInputStream(imageHeader))
			.setParameter("IMAGE_DETAIL", new ByteArrayInputStream(imageDetail));
			reportHandler.exportReport(input, bufferedOut);
		} catch (Exception e) {
			throw ErrorHelper.exception(e);
		} finally {
			// Wajib dibersihkan agar file temp di disk terhapus
			virtualizer.cleanup();
		}
	}
	
}
