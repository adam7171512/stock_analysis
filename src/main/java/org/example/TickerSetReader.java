package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TickerSetReader {

    private static final String SWIG80_FILE_PATH = "src/main/resources/gpw/swig80.txt";
    private static final String MWIG40_FILE_PATH = "src/main/resources/gpw/mwig40.txt";
    private static final String WIG20_FILE_PATH = "src/main/resources/gpw/wig20.txt";

    public static Set<String> getSWig80Tickers() throws IOException {
        return getTickersFromFile(SWIG80_FILE_PATH);
    }

    public static Set<String> getMWig40Tickers() throws IOException {
        return getTickersFromFile(MWIG40_FILE_PATH);
    }

    public static Set<String> getWig20Tickers() throws IOException {
        return getTickersFromFile(WIG20_FILE_PATH);
    }


    private static Set<String> getTickersFromFile(String filePath) throws IOException {
        Set<String> result = new HashSet<>();
        File file = new File(filePath);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split(" ", 2);
            if (splitLine.length > 0 && !splitLine[0].isEmpty()) {
                result.add(splitLine[0]);
            }
        }
        br.close();

        return result;
    }
}
