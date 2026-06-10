package com.sabtok.process.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.sabtok.process.dto.BookRecord;
import com.sabtok.process.dto.PageResponseRecord;
import com.sabtok.process.openfeign.SabInfoClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookWriteServiceV2 {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final SabInfoClient sabInfoClient;
    private final PageWriterService pageWriterService;
    final Map<String, Set<PageResponseRecord>> notes = new ConcurrentHashMap<>();

    // Set up basic typography
    Font mainTitleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
    Font bookTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
    Font pageTitleFont = new Font(Font.HELVETICA, 10, Font.UNDERLINE);
    Font pageBodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

    public Object writeBook() throws IOException {

        final Set<BookRecord> books  = getAllBooks();
        System.out.println(Thread.currentThread().getName()+" Total number of books "+books.size());
        Set<BookRecord> uniqueBooks = new HashSet<>(books.stream()
                .filter(book -> book.bookName() != null)
                .collect(Collectors.toMap(
                        BookRecord::bookName,               // Map key for uniqueness
                        book -> book,                       // Map value
                        (existing, replacement) -> existing // Merge rule: keep the first instance found
                ))
                .values());
        System.out.println(Thread.currentThread().getName()+" Getting pages");
        final Map<String, Set<PageResponseRecord>> notes = getAllPagesForBook(uniqueBooks);
        //Map<String, Set<PageResponseRecord>> notes = getAllPagesForBook(books);
        return writeToPdf_old(notes);
    }

    public byte[] writeToPdf(Map<String, Set<PageResponseRecord>> notes) throws IOException {
        Document document = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            //PdfWriter.getInstance(document, out);
            document.open();
            for (Map.Entry<String, Set<PageResponseRecord>> entry : notes.entrySet()) {
                String bookName = entry.getKey();
                Set<PageResponseRecord> pages = entry.getValue();
                // Append Book Section header
                document.add(new Paragraph("Book: " + bookName, bookTitleFont));
                List<CompletableFuture<Void>> pagesFutures = new ArrayList<>();
                for (PageResponseRecord page : pages) {
                    CompletableFuture<Void> pageFuture = CompletableFuture.runAsync(()-> {
                        document.add(new Paragraph(page.title(), pageTitleFont));
                        System.out.println(Thread.currentThread().getName()+" getting data for page : "+page.title());
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
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                // CRITICAL FIX 1: Explicitly close the reader to drop references to the underlying byte array
                                if (reader != null) {
                                    reader.close();
                                    // CRITICAL FIX 2: Clear internal iText cache structures for this specific reader instance
                                    try {
                                        writer.freeReader(reader);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        document.add(new Paragraph("\n"));
                    }, executor);
                    pagesFutures.add(pageFuture);
                }
                pagesFutures.forEach(CompletableFuture::join);
                document.add(new Paragraph("\n"));
            }
            document.close();
            return out.toByteArray();
        }
    }

    public byte[] writeToPdf_old(Map<String, Set<PageResponseRecord>> notes) throws IOException {

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
            document.add(new Paragraph(Thread.currentThread().getName()+" Library Catalog Report", mainTitleFont));
            document.add(new Paragraph(Thread.currentThread().getName()+" Generated on: " + new Date() + "\n\n", pageBodyFont));

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
                        System.out.println(Thread.currentThread().getName()+" getting data for page : "+page.title());
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
                    document.add(new Paragraph("\n"));
                }
                document.add(new Paragraph("\n"));
            }

            // Close document to compile trailing memory buffers
            document.close();
            return out.toByteArray();
        }
    }

    private Map<String, Set<PageResponseRecord>> getAllPagesForBook(final Set<BookRecord> books ) {
        System.out.println(Thread.currentThread().getName()+" starting loop");
        final List<CompletableFuture<Void>> bookFutures = new ArrayList<>();
        for (BookRecord book : books) {
            CompletableFuture<Void> bookFuture = CompletableFuture.runAsync(() -> {
                String workerThread = Thread.currentThread().getName();
                System.out.println(workerThread + " : book name : " + book.bookName());
                // 1. Fetch pages synchronously inside the book thread
                System.out.println(workerThread + " Getting page list for the book "+book.bookName()+" book id : "+book.bookId());
                Set<PageResponseRecord> pages = sabInfoClient.getAllPagesForBook(book.bookId());
                System.out.println(workerThread+" Total number of pages to process: " + pages.size());
                // 4. Thread-safe save to the map after all pages are completely processed
                notes.put(book.bookName(), pages);
                System.out.println(workerThread+" Added pages to notes "+pages.size());
            }, executor); // Pass your custom executor here
            bookFutures.add(bookFuture);
            System.out.println(Thread.currentThread().getName() + "added into future");
        }
        System.out.println(Thread.currentThread().getName() + " completed loop");
        bookFutures.forEach(CompletableFuture::join); // Wait for all book threads to finish
        System.out.println(Thread.currentThread().getName()+" Request completed ...!");
        return notes;
    }

    private Set<BookRecord> getAllBooks() {
        return sabInfoClient.getAllBooks();
    }

}
