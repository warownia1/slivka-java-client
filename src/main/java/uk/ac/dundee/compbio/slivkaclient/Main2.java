package uk.ac.dundee.compbio.slivkaclient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static java.lang.String.format;

public class Main2 {

  public static void main(String[] args) throws IOException, FormValidationException, InterruptedException {
    long time = System.currentTimeMillis();
    SlivkaClient client = new SlivkaClient(URI.create("http://www.compbio.dundee.ac.uk/slivka/"));
    SlivkaService service = client.getService("example");
    SlivkaForm form = service.getForm();
//    for (FormField field : form.getFields()) {
//      System.out.println(format("%s: %s", field.getName(), field.getLabel()));
//    }
//    System.out.println(System.currentTimeMillis() - time);
//    form.insert("sleep", 10);
    form.insert("content", "foobar");
    form.insert("extra-count", 5);
    RemoteFile rf = client.uploadFile(new File("/bin/bash"), "application/octet-stream");
    rf.dump(System.out);
    form.insert("input-file", client.uploadFile(new File("/home/mmwarowny/Documents/example.json"), "application/octet-stream"));
    String jobId = form.submit();
    JobState state;
    while ((state = client.getJobState(jobId)) != JobState.COMPLETED) {
      Thread.sleep(1000);
    }
    List<RemoteFile> files = client.getJobResults(jobId);
    for (RemoteFile file : files) {
      System.out.println("---" + file.getTitle() + "---");
      file.dump(System.out);
    }
  }
  
}
