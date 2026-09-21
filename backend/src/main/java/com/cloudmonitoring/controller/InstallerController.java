package com.cloudmonitoring.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Controller to serve automated installation scripts and the monitoring agent bundle
 * to target host machines (Windows, Linux, Raspberry Pi, AWS EC2).
 */
@RestController
public class InstallerController {

    private final Path projectRoot = Paths.get("..").toAbsolutePath().normalize();

    @GetMapping(value = "/install.ps1", produces = "text/plain")
    public ResponseEntity<Resource> getWindowsInstaller() throws IOException {
        Path scriptPath = findAgentFile("install.ps1");
        if (scriptPath != null && Files.exists(scriptPath)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"install.ps1\"")
                    .body(new FileSystemResource(scriptPath));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/install.sh", produces = "text/plain")
    public ResponseEntity<Resource> getLinuxInstaller() throws IOException {
        Path scriptPath = findAgentFile("install.sh");
        if (scriptPath != null && Files.exists(scriptPath)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"install.sh\"")
                    .body(new FileSystemResource(scriptPath));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/agent.zip", produces = "application/zip")
    public ResponseEntity<Resource> getAgentZipBundle() throws IOException {
        Path agentDir = findAgentDirectory();
        if (agentDir == null || !Files.exists(agentDir)) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zipDirectory(agentDir.toFile(), agentDir.toFile().getName(), zos);
        }

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"agent.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(baos.size())
                .body(resource);
    }

    private Path findAgentFile(String filename) {
        Path p1 = Paths.get("monitoring-agent", filename);
        if (Files.exists(p1)) return p1;

        Path p2 = Paths.get("..", "monitoring-agent", filename);
        if (Files.exists(p2)) return p2;

        return null;
    }

    private Path findAgentDirectory() {
        Path p1 = Paths.get("monitoring-agent");
        if (Files.exists(p1) && Files.isDirectory(p1)) return p1;

        Path p2 = Paths.get("..", "monitoring-agent");
        if (Files.exists(p2) && Files.isDirectory(p2)) return p2;

        return null;
    }

    private void zipDirectory(File folderToZip, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folderToZip.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().startsWith(".") || file.getName().equals("__pycache__") || file.getName().endsWith(".pyc")) {
                continue;
            }
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
            }
        }
    }
}