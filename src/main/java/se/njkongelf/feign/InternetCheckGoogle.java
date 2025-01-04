package se.njkongelf.feign;

import feign.RequestLine;

public interface InternetCheckGoogle {

  @RequestLine("GET")
  String internetcheck();
}
