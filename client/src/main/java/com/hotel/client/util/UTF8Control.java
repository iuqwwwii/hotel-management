package com.hotel.client.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class UTF8Control extends ResourceBundle.Control {
    @Override
    public ResourceBundle newBundle(
            String baseName, Locale locale, String format, ClassLoader loader, boolean reload
    ) throws java.io.IOException {
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        InputStream stream = loader.getResourceAsStream(resourceName);
        if (stream == null) return null;
        try (InputStreamReader isr = new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(isr);
        }
    }
}

