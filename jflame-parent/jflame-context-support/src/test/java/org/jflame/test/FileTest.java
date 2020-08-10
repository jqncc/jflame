package org.jflame.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

public class FileTest {

    @Test
    public void test() {
        try {
            URI u = this.getClass()
                    .getResource("/")
                    .toURI();
            Path path = Paths.get(u)
                    .resolve("a.cache");
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.write(path, String.valueOf(6)
                    .getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

}
