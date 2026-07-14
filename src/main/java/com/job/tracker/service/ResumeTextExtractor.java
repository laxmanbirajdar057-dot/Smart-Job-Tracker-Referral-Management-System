package com.job.tracker.service;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Extracts plain text from a resume file (PDF/DOCX stored as bytes in Resume.cvFile)
 * using Apache Tika. Shared by MatchScoreService and AtsCheckerService so text
 * extraction logic lives in exactly one place.
 */
@Service
public class ResumeTextExtractor {

    public String extractText(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            return "";
        }
        try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
            // -1 disables Tika's default ~100k character truncation limit
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from resume file", e);
        }
    }
}
