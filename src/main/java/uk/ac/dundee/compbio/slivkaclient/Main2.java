package uk.ac.dundee.compbio.slivkaclient;

import uk.ac.dundee.compbio.slivkaclient.http.HttpClientWrapper;
import uk.ac.dundee.compbio.slivkaclient.http.HttpResponse;
import uk.ac.dundee.compbio.slivkaclient.http.impl.ApacheHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main2 {

  public static void main(String[] args) throws IOException {
    HttpClientWrapper client = ApacheHttpClient.create();

    Consumer<HttpResponse> doneCallback = httpResponse -> {
      System.out.println(httpResponse.getStatusCode());
      try {
        System.out.println(httpResponse.getText());
      } catch (IOException ignored) {
      }
    };
    BiConsumer<HttpResponse, Throwable> failCallback = (httpResponse, throwable) -> {
      if (httpResponse != null) System.out.println(httpResponse.getStatusCode());
      if (throwable != null) throwable.printStackTrace();
    };

    client.post(URI.create("https://ptsv2.com/t/mmw/post"))
        .addParameter("Hello", "world")
        .executeAsync(doneCallback, failCallback, null);
  }
}
