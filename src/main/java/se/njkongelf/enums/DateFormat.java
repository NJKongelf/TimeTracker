package se.njkongelf.enums;

import lombok.Getter;

@Getter
public enum DateFormat {
  DATE_FORMAT ("yyyy-MM-dd"),
  TIME_FORMAT ( "HH:mm:ss");
  private final String code;

  DateFormat(String code) {
    this.code = code;
  }
}
