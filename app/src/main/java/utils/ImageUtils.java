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

    //scale down the image size
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
}
