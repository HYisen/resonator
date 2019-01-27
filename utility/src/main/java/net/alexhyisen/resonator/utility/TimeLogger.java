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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("WeakerAccess")
public class TimeLogger {
    private Instant first;
    private Instant last;
    private String id;
    private Path path;

    public static TimeLogger one = new TimeLogger("_one");

    public TimeLogger(String id) {
        this.id = id;
        this.path = Paths.get("output", id + "_log");
        restart();
    }

    private boolean isFileOutput() {
        return !id.startsWith("_");
    }

    public void restart(){
        first = Instant.now();
        last = first;
        if (isFileOutput()) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        log("T0");
    }

    public void log(String msg){
        Instant now = Instant.now();
        String content = String.format("%s : +%10d,%10d ms %s\n", toString(),
                last.until(now, ChronoUnit.MILLIS),
                first.until(now, ChronoUnit.MILLIS),
                msg);
        last = now;
        System.out.print(content);
        if (isFileOutput()) {
            try {
                Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void print(String msg) {
        System.out.print(msg);
        try {
            var path = Paths.get(".", "log");
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.write(
                    path,
                    msg.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Stopwatch " + id;
    }
}