package uk.ac.dundee.compbio.slivkaclient;

import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import static java.lang.String.format;

@SuppressWarnings("serial")
public class Main extends JFrame {
 
  SlivkaClient client = new SlivkaClient(
      URI.create("http://www-dev.compbio.dundee.ac.uk/slivka/")
      );
  JFileChooser fc = new JFileChooser();
  
  JTextArea outputText;
  JButton fileButton = new JButton("open...");
  JTextField contentInput = new JTextField();
  JSpinner countInput = new JSpinner();
  
  public Main() throws HeadlessException {
    super("Slivka test");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setSize(800, 600);
    this.setVisible(true);
    this.setLayout(new GridLayout(1, 2));
    
    JComponent controls = new Box(BoxLayout.Y_AXIS);
    JComponent form = new JPanel();
    form.setLayout(new GridLayout(3, 2));
    form.add(new JLabel("input-file"));
    fileButton.addActionListener(this::openFileDialog);
    form.add(fileButton);
    form.add(new JLabel("content"));
    form.add(contentInput);
    form.add(new JLabel("extra-count"));
    form.add(countInput);
    controls.add(form);
    
    JComponent buttons = new Box(BoxLayout.X_AXIS);
    JButton getButton = new JButton("GET");
    JButton postButton = new JButton("POST");
    getButton.addActionListener(this::getButtonAction);
    postButton.addActionListener(this::postButtonAction);
    buttons.add(getButton);
    buttons.add(postButton);
    controls.add(buttons);
    
    this.add(controls);
    
    outputText = new JTextArea();  
    outputText.setEditable(false);
    this.add(outputText);
    this.getContentPane().doLayout();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Main::new);
  }
  
  private void openFileDialog(ActionEvent event) {
    int status = fc.showOpenDialog(getContentPane());
    JButton button = (JButton)event.getSource();
    if (status == JFileChooser.APPROVE_OPTION) {
      button.setText(fc.getSelectedFile().getName());
    }
    else {
      button.setText("open...");
    }
  }
  
  private void getButtonAction(ActionEvent event) {
    outputText.append("[LOG] Requesting service information\n");
    SlivkaService service;
    try {
      service = client.getService("example");
      outputText.append(format("[OK] Service found: %s%n", service.label));
    }
    catch (IOException e) {
      outputText.append(format("[ERROR] Connection error %s%n", e.toString()));
      e.printStackTrace();
    }
  }
  
  private void postButtonAction(ActionEvent event) {
    outputText.append("[LOG] Posting data to the server\n");
    File file = fc.getSelectedFile();
    if (file != null)
      outputText.append(format("[LOG] input-file: %s%n", file.getAbsolutePath()));
    else
      outputText.append("[LOG] input-file: null\n");
    outputText.append(format("[LOG] content: \"%s\"%n", contentInput.getText()));
    outputText.append(format("[LOG] extra-count: %s%n", countInput.getValue().toString()));
    try {
      SlivkaService service = client.getService("example");
      SlivkaForm form = service.getForm();
      if (file != null) {
        RemoteFile rf = client.uploadFile(file, "text/plain");
        form.insert("input-file", rf);
      }
      form.insert("content", contentInput.getText());
      form.insert("extra-count", (Integer)countInput.getValue());
      String id = form.submit();
      outputText.append(format("[OK] Job submitted: %s%n", id));
      new Timer(1000, (evt) -> {
        JobState state;
        try {
          state = client.getJobState(id);
          outputText.append(format("[LOG] Job state: %s%n", state.name()));
          if (state == JobState.COMPLETED) {
            ((Timer)evt.getSource()).stop();
            outputText.append("[OK] Success");
          }
        } catch (IOException e) {
          outputText.append(format("[ERROR] Connection error %s%n", e.toString()));
        }
      }).start();
    }
    catch (IOException e) {
      outputText.append(format("[ERROR] Connection error %s%n", e.toString()));
      e.printStackTrace();
    }
    catch (FormValidationException e) {
      outputText.append("[ERROR] Invalid input data\n");
      for (ValidationException exc : e.getErrors()) {
        outputText.append(format("[ERROR] %s: %s%n", exc.getField().getName(), exc.getMessage()));
      }
      e.printStackTrace();
    }
  }

}
