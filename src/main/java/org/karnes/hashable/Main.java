package org.karnes.hashable;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static spark.Spark.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Path contentPath = Paths.get("content");

    public static void main(String[] args) {
        //Serve existing files.
        staticFiles.externalLocation(contentPath.toAbsolutePath().toString());

        //Accept new content through POSTs
        post("/", (req, res) -> processRequest(req, res));

    }

    private static Object processRequest(Request req, Response res) {
        byte[] bytes = req.bodyAsBytes();

        String hash = hashBytes(bytes);

        saveFile(hash, bytes);

        return hash;
    }

    private static File saveFile(String hash, byte[] bytes) {
        Path filePath = contentPath.resolve(hash);
        File file = filePath.toFile();
        if (file.exists()) {
            logger.info("The contents already exist at: {}", filePath.toAbsolutePath());
        } else {
            logger.debug("File does not already exist.");
            try {
                Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (IOException e) {
                logger.error("Unable to save content to file.", e);
                halt(500, "Unable to save content to file.");
            }
            logger.info("Saved content to file: {}", filePath.toAbsolutePath());
        }
        return file;
    }

    private static String hashBytes(byte[] bytes) {
        SHA3Digest md = new SHA3Digest(512);
        md.reset();
        md.update(bytes, 0, bytes.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        String hexHash = DatatypeConverter.printHexBinary(digest);
        logger.debug("Hash: " + hexHash);
        return hexHash;
    }
}
