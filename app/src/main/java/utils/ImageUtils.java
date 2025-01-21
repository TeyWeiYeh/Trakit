package utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static Bitmap editImgSize(Bitmap bitmap, int width, int height){
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

//    public static Bitmap resizeBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        // Calculate the aspect ratio
//        float aspectRatio = (float) width / height;
//
//        // If both targetWidth and targetHeight are zero, return the original bitmap
//        if (targetWidth <= 0 && targetHeight <= 0) {
//            return bitmap;
//        }
//
//        // Calculate new dimensions based on the provided width or height
//        int newWidth;
//        int newHeight;
//
//        if (targetWidth > 0 && targetHeight > 0) {
//            // Use the smallest scaling factor to ensure the image fits within the bounds
//            float scale = Math.min((float) targetWidth / width, (float) targetHeight / height);
//            newWidth = Math.round(width * scale);
//            newHeight = Math.round(height * scale);
//        } else if (targetWidth > 0) {
//            // Scale width and calculate height
//            newWidth = targetWidth;
//            newHeight = Math.round(targetWidth / aspectRatio);
//        } else {
//            // Scale height and calculate width
//            newHeight = targetHeight;
//            newWidth = Math.round(targetHeight * aspectRatio);
//        }
//
//        // Create and return the scaled bitmap
//        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
//    }

    public static Bitmap scaleToTargetSize(Bitmap bitmap, long maxSizeBytes, Bitmap.CompressFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        long currentSize = stream.size();

        // If already smaller than target size, return original
        if (currentSize <= maxSizeBytes) {
            return bitmap;
        }

        // Calculate scale factor based on sqrt of size ratio
        // This works because image size is roughly proportional to area
        double scaleFactor = Math.sqrt((double) maxSizeBytes / currentSize);

        int newWidth = Math.round((float) (bitmap.getWidth() * scaleFactor));
        int newHeight = Math.round((float) (bitmap.getHeight() * scaleFactor));

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate the aspect ratio
        float aspectRatio = (float) width / height;

        // If both targetWidth and targetHeight are zero, return the original bitmap
        if (targetWidth <= 0 && targetHeight <= 0) {
            return bitmap;
        }

        // Calculate new dimensions based on the provided width or height
        int newWidth;
        int newHeight;

        if (targetWidth > 0 && targetHeight > 0) {
            // Use the smallest scaling factor to ensure the image fits within the bounds
            float scale = Math.min((float) targetWidth / width, (float) targetHeight / height);
            newWidth = Math.round(width * scale);
            newHeight = Math.round(height * scale);
        } else if (targetWidth > 0) {
            // Scale width and calculate height
            newWidth = targetWidth;
            newHeight = Math.round(targetWidth / aspectRatio);
        } else {
            // Scale height and calculate width
            newHeight = targetHeight;
            newWidth = Math.round(targetHeight * aspectRatio);
        }

        // Create and return the scaled bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
