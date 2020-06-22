package uk.ac.dundee.compbio.slivkaclient.http.impl;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;

public class HttpClientFactory {

  public static HttpClientWrapper getDefault() {
    return ApacheHttpClient.create();
  }

}
