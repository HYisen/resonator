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

import java.util.List;
import java.util.stream.Collectors;

public class Utility {
    public static Runnable NOOP = () -> {
    };

    public static String format(String pattern, List<Pair<String, String>> dictionary) {
        return pattern
                .lines()
                .map(line -> {
                    for (var pair : dictionary) {
                        line = line.replace("${" + pair.getFirst() + "}", pair.getSecond());
                    }
                    return line;
                })
                .collect(Collectors.joining("\n"));
    }
}
