package com.rxlogix.util

final class Strings {
  // Given an input String, collapses each contiguous occurrance of whitespace to a single space. Also removes
  // leading or trailing whitespace.
  static String collapseWhitespace(final String it) {
    it.replaceAll( /\s+/, " ").trim();
  }

  // Given an input String, truncates all chars beyond maxLen 
  static String trunc(final String it, final int maxLen) {
    if (it == null) {
      return null;
    }
    it.substring( 0, Math.min(it.length(), maxLen) );
  }

    def static breakIt(String income) {
        income.split("(?=\\p{Upper})").join(' ')
    }
}
