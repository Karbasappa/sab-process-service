package com.sabtok.process.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.lowagie.text.*;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.PageResponseRecord;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class BookWriterService {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final SabInfoClient sabInfoClient;
    private final PageWriterService pageWriterService;

    @SneakyThrows
    public byte[] writeBook() {
        Set<BookRecord> books  = getAllBooks();
        Map<String, Set<PageResponseRecord>> notes = getAllPagesForBook(books.stream().findFirst().map(Set::of).orElse(Collections.emptySet()));
        //Map<String, Set<PageResponseRecord>> notes = getAllPagesForBook(books);
        return writeToPdf(notes);
    }

    @SneakyThrows
    public byte[] writeToPdf(Map<String, Set<PageResponseRecord>> notes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Initialize PDF framework
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            //PdfWriter.getInstance(document, out);
            document.open();

            // Set up basic typography
            Font mainTitleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font bookTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font pageTitleFont = new Font(Font.HELVETICA, 10, Font.UNDERLINE);
            Font pageBodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            // Document Header
            document.add(new Paragraph("Library Catalog Report", mainTitleFont));
            document.add(new Paragraph("Generated on: " + new Date() + "\n\n", pageBodyFont));

            // Iterate and write map elements sequentially into the PDF structure
            for (Map.Entry<String, Set<PageResponseRecord>> entry : notes.entrySet()) {
                String bookName = entry.getKey();
                Set<PageResponseRecord> pages = entry.getValue();

                // Append Book Section header
                document.add(new Paragraph("Book: " + bookName, bookTitleFont));

                // Append child list details
                if (pages == null || pages.isEmpty()) {
                    document.add(new Paragraph("  (No pages found for this book)", pageBodyFont));
                } else {
                    for (PageResponseRecord page : pages) {
                        document.add(new Paragraph("  • " + page.title(), pageTitleFont));
                        System.out.println("getting data for page : "+page.title());
                        byte [] pdfBytes = pageWriterService.writePage(page.pageId());
                        if (pdfBytes != null && pdfBytes.length > 0) {
                            PdfReader reader = null;
                            try {
                                reader = new PdfReader(pdfBytes);
                                int totalPages = reader.getNumberOfPages();
                                PdfContentByte cb = writer.getDirectContent();

                                for (int i = 1; i <= totalPages; i++) {
                                    document.newPage();
                                    PdfImportedPage importedPage = writer.getImportedPage(reader, i);
                                    cb.addTemplate(importedPage, 1.0f, 0, 0, 1.0f, 0, 0);
                                }
                            } finally {
                                // CRITICAL FIX 1: Explicitly close the reader to drop references to the underlying byte array
                                if (reader != null) {
                                    reader.close();
                                    // CRITICAL FIX 2: Clear internal iText cache structures for this specific reader instance
                                    writer.freeReader(reader);
                                }
                            }
                        }
                    }
                }
                document.add(new Paragraph("\n"));
            }

            // Close document to compile trailing memory buffers
            document.close();
            return out.toByteArray();
        }
    }

    private Set<BookRecord> getAllBooks() {
        return sabInfoClient.getAllBooks();
    }

    private Map<String, Set<PageResponseRecord>> getAllPagesForBook(Set<BookRecord> books ) {
        List<CompletableFuture<Void>> bookFutures = new ArrayList<>();
        Map<String, Set<PageResponseRecord>> notes = new ConcurrentHashMap<>();
        for (BookRecord book : books) {
            CompletableFuture<Void> bookFuture = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName() + " : book name : " + book.bookName());

                // 1. Fetch pages synchronously inside the book thread
                Set<PageResponseRecord> pages = sabInfoClient.getAllPagesForBook(book.bookId());
                System.out.println("Total number of pages to process: " + pages.size());


                // 4. Thread-safe save to the map after all pages are completely processed
                notes.put(book.bookName(), pages);

            }, executor); // Pass your custom executor here

            bookFutures.add(bookFuture);
        }
        bookFutures.forEach(CompletableFuture::join); // Wait for all book threads to finish
        System.out.println("Request completed ...!");
        return notes;
    }

    public byte[] generatePdfFromImageString(String base64ImageText) throws IOException {
        // 1. Clean the string if it contains browser data URI prefixes
        if (base64ImageText.contains(",")) {
            base64ImageText = base64ImageText.substring(base64ImageText.indexOf(",") + 1);
        }

        // 2. Decode the raw Base64 string back into actual image bytes
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64ImageText.trim());
        } catch (IllegalArgumentException e) {
            throw new IOException("The provided text string is not a valid Base64 format.", e);
        }

        // 3. Prepare the PDF Document
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // 4. Create an OpenPDF Image element from the decoded bytes
            Image pdfImage = Image.getInstance(imageBytes);

            // 5. Scale the image safely to fit within standard A4 margins
            float printableWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float printableHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();

            pdfImage.scaleToFit(printableWidth, printableHeight);
            pdfImage.setAlignment(Image.ALIGN_CENTER);

            // 6. Write the image element into the document pipeline
            document.add(pdfImage);

        } catch (Exception e) {
            throw new IOException("Failed to assemble PDF structure from image string source.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream.toByteArray();
    }
}
