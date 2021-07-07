package uk.ac.dundee.compbio.slivkaclient;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;

public class JobRequest
{
  protected ArrayList<Map.Entry<String, String>> data = new ArrayList<>();
  protected ArrayList<Map.Entry<String, File>> files = new ArrayList<>();
  protected ArrayList<Map.Entry<String, InputStream>> streams = new ArrayList<>();
  
  public void addData(String key, Object value) {
    data.add(new AbstractMap.SimpleEntry<>(key, value.toString()));
  }
  
  public void addFile(String key, File value) {
    files.add(new AbstractMap.SimpleEntry<>(key, value));
  }
  
  public void addFile(String key, InputStream value) {
    streams.add(new AbstractMap.SimpleEntry<>(key, value));
  }
  
  List<Map.Entry<String, String>> getData() {
    return data;
  }
  
  List<Map.Entry<String, File>> getFiles() {
    return files;
  }
  
  List<Map.Entry<String, InputStream>> getStreams() {
    return streams;
  }
}
