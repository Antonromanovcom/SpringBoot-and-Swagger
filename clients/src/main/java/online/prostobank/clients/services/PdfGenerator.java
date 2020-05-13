package online.prostobank.clients.services;

import com.itextpdf.text.pdf.BaseFont;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

@Component
public class PdfGenerator {

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PdfGenerator.class);

	public void createPdf(TemplateEngine templateEngine, String templateName, Map<String, Object> map, OutputStream os) throws Exception {
		Context ctx = new Context();
		Iterator itMap = map.entrySet().iterator();
		while (itMap.hasNext()) {
			Map.Entry pair = (Map.Entry) itMap.next();
			ctx.setVariable(pair.getKey().toString(), pair.getValue());
		}

		String processedHtml = templateEngine.process(templateName, ctx);
		LOG.info(processedHtml);
		ITextRenderer renderer = new ITextRenderer();
		renderer.getFontResolver().addFont("/fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		renderer.setDocumentFromString(processedHtml);
		renderer.layout();
		renderer.createPDF(os, false);
		renderer.finishPDF();
	}
}
