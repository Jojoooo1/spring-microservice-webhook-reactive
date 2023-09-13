package com.service.webhook.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class UrlUtils {

  // Using Regular Expression provided by OWASP
  private static final String URL_REGEX =
      "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))"
          + "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)"
          + "([).!';/?:,][[:blank:]])?$";

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public static boolean isValid(final String url) {
    if (!StringUtils.hasText(url)) {
      return false;
    }

    final Matcher matcher = URL_PATTERN.matcher(url);
    return matcher.matches();
  }
}
