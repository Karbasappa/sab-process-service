package com.sabtok.process.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.sabtok.process.dto.PageResponseRecord;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PageWriterService {

    private final SabInfoClient sabInfoClient;

    public byte[] writePage(String pageId){
        // 1. Guard against null or missing pageId inputs
        if (pageId == null || pageId.trim().isEmpty()) {
            throw new IllegalArgumentException("The pageId input parameter cannot be null or empty.");
        }

        PageResponseRecord pageDetails = sabInfoClient.getPageDetails(pageId);

        // 2. Prevent NullPointerExceptions if the client returns empty records
        if (pageDetails == null || pageDetails.content() == null) {
            throw new IllegalStateException("Failed to retrieve content for pageId: " + pageId);
        }

        String htmlString = pageDetails.content();

        // 3. Wrap in a try-with-resources statement to auto-close the memory stream safely
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 4. Transform the HTML structure right into the memory buffer
            HtmlConverter.convertToPdf(htmlString, baos);

            // 5. Return the raw byte array
            return baos.toByteArray();

        } catch (IOException e) {
            // Log the error and bubble up a clear runtime exception
            System.err.println("Critical error compiling PDF bytes for pageId: " + pageId);
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF document from HTML string source", e);
        }
    }

    /*

    public byte[] writePageOld(String pageId) throws IOException {
    PageResponseRecord pageDetails = sabInfoClient.getPageDetails(pageId);
    String htmlContent = pageDetails.content();
    String rawHtmlContent = convertStringToCleanHtml(htmlContent);

    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(rawHtmlContent);
    jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
    //jsoupDoc.outputSettings().prettyPrint(false); // Keeps data-strings intact
    String strictXhtml = jsoupDoc.html();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();

        // 2. Pass strict XHTML directly (No external custom element factory required!)
        builder.withHtmlContent(strictXhtml, null);
        builder.toStream(outputStream);

        // 3. Process components and close buffers
        builder.run();

    } catch (Exception e) {
        throw new IOException("Failed processing XHTML tokens into PDF stream paths", e);
    }

    return outputStream.toByteArray();
}
     */
}
