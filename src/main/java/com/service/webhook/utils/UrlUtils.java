package com.service.webhook.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtils {

  public static boolean isValid(final String url) {
    try {
      new URL(url).toURI();
      return true;
    } catch (final MalformedURLException | URISyntaxException e) {
      return false;
    }
  }
}
