package com.sgf.eventport.complier.utlis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

public class FileUtils {

    public static String readForFile(File file) throws IOException {

        if (file == null || !file.exists()) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String readLineStr;
            while ((readLineStr = bufferedReader.readLine()) != null) {
                buffer.append(readLineStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static void writeInFile(File file, String content) throws IOException {
        Writer writer = null;
        StringBuilder outputString = new StringBuilder();
        try {
            outputString.append(content);
            writer = new FileWriter(file, false);
            writer.write(outputString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
