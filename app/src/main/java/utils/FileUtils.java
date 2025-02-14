package utils;

import android.util.Base64;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
    //encode and decode the excel file that is passed between server and client as base64 string
    public static String encodeFileToBase64(HSSFWorkbook workbook) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos); // Write workbook content to ByteArrayOutputStream
            workbook.close(); // Close workbook to prevent memory leaks

            byte[] workbookBytes = bos.toByteArray();
            return Base64.encodeToString(workbookBytes, Base64.DEFAULT); // Encode to Base64
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //decode the base64 string file
    public static HSSFWorkbook decodeBase64ToWorkbook(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
            return new HSSFWorkbook(bis); // Create HSSFWorkbook from input stream
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
