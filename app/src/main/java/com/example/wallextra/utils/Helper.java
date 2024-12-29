package com.example.wallextra.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class Helper {
    public static String getFileExtension(Context context, Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(
                context.getContentResolver().getType(uri));
    }
}
