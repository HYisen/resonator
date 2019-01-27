/*
 * This file is part of resonator.
 *
 * resonator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * resonator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with resonator.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.alexhyisen.resonator.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Alex on 2017/3/6.
 * Config is a configs' serialization solution.
 */

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Config {
    private Map<String, String> data = new LinkedHashMap<>();
    private final Path path;

    public Config() {
        this.path = Paths.get(".", "config");
    }

    //That explicit constructor is only used for redirect path in Test s
    public Config(Path path) {
        this.path = path;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String get(String key) {
        return data.get(key);
    }

    public String put(String key, String value) {
        return data.put(key, value);
    }

    public boolean save(Path path) {
        //data.forEach((k,v)-> System.out.println(k+"="+v));
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.deleteIfExists(path);
            Files.write(path, gson.toJson(data).getBytes(), StandardOpenOption.CREATE_NEW);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean save() {
        return save(path);
    }

    public boolean load(Path path) {
        data = new LinkedHashMap<>();//I want to keep the config content in insert order
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            data = gson.fromJson(new String(Files.readAllBytes(path)), new TypeToken<Map<String, String>>() {
            }.getType());
            //data.forEach((k,v)-> System.out.println(k+"="+v));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean load() {
        return load(path);
    }
}

