package com.learnspherex.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    // Returns a relative path (e.g. "materials/<uuid>_name.pdf") to store as the
    // record's fileUrl - never the client-supplied name alone, to avoid collisions
    // and path traversal.
    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "A file is required");
        String original = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName().toString();
        String stored = UUID.randomUUID() + "_" + original;
        try {
            Path dir = root.resolve(subDir).normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(stored);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file: " + ex.getMessage());
        }
        return subDir + "/" + stored;
    }

    public byte[] load(String relativePath) {
        try {
            Path path = root.resolve(relativePath).normalize();
            if (!path.startsWith(root))
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file path");
            if (!Files.exists(path))
                throw new ApiException(HttpStatus.NOT_FOUND, "File not found on disk: " + relativePath);
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file: " + ex.getMessage());
        }
    }
}
