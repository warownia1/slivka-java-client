package uk.ac.dundee.compbio.slivkaclient;

public class SlivkaVersion
{
  public final String slivka, api;
  SlivkaVersion(String slivka, String api) {
    this.slivka = slivka;
    this.api = api;
  }
  
  public String toString() {
    return String.format("SlivkaVersion(slivka=%s, api=%s)", slivka, api);
  }
}
