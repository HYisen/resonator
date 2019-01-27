package net.alexhyisen.resonator.utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Alex on 2017/3/12.
 * Test whether Config works properly.
 */

class ConfigTest {
    private Path path;
    private Map<String, String> data;

    @BeforeEach
    void setUp() throws Exception {
        data = new LinkedHashMap<>();
        data.put("client", "localhost");
        data.put("server", "smtp.163.com");
        data.put("username", "sender@163.com");
        data.put("password", "how_would_I_tell_you");
        data.put("senderName", "SMTP_Tester");
        data.put("senderAddr", "sender@163.com");
        data.put("recipientName", "Receiver");
        data.put("recipientAddr", "receiver@gmail.com");

        path = Paths.get(".", "test_config");
    }

    @Test
    void persistence() {
        Config saveConfig = new Config(path);
        data.forEach(saveConfig::put);
        saveConfig.save();

        Config loadConfig = new Config(path);
        loadConfig.load();
        data.forEach((key, value) -> {
            //System.out.println("check "+key+" : "+loadConfig.get(key)+" = "+value);
            assert value.equals(loadConfig.get(key));
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.delete(path);
    }
}