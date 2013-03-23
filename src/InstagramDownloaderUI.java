import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class InstagramDownloaderUI
  extends JFrame
  implements ActionListener
{
  JTextField htField;
  JButton toggle;
  JButton choose;
  JFileChooser fileChooser;
  JLabel chosenOutputLabel;
  JCheckBox debugChkBox;
  Boolean running = Boolean.valueOf(false);
  String RUN_TEXT = "Run";
  String STOP_TEXT = "Stop";
  String DEFAULT_TAG = "mvsstudio";
  File outputDir;
  RequestMgr reqMgr;
  
  public InstagramDownloaderUI() {
    initComponents();
    setDefaultCloseOperation(3);
    setTitle("Instaprint - MVS");
  } 
  
  private void initComponents()
  {
    setLayout(null);
    
    JLabel label = new JLabel();
    label.setText("Hashtag:");
    
    JLabel outputLabel = new JLabel();
    outputLabel.setText("Output:");
    
    this.chosenOutputLabel = new JLabel();
    this.chosenOutputLabel.setText("");
    
    this.htField = new JTextField();
    this.htField.setText(this.DEFAULT_TAG);
    
    this.toggle = new JButton();
    this.toggle.setActionCommand("toggle");
    this.toggle.setText(this.RUN_TEXT);
    this.toggle.addActionListener(this);
    
    this.choose = new JButton();
    this.choose.setActionCommand("chooseDir");
    this.choose.setText("...");
    this.choose.addActionListener(this);
    
    this.fileChooser = new JFileChooser();
    this.fileChooser.setFileSelectionMode(1);
    JLabel debugLabel = new JLabel();
    debugLabel.setText("Debug:");
    debugLabel.setBounds(20, 250, 60, 20);
    add(debugLabel);
    this.debugChkBox = new JCheckBox();
    this.debugChkBox.setBounds(80, 250, 20, 20);
    add(this.debugChkBox);
    
    label.setBounds(20, 10, 70, 20);
    add(label);
    
    this.htField.setBounds(100, 10, 230, 20);
    add(this.htField);
    
    outputLabel.setBounds(20, 40, 80, 20);
    add(outputLabel);
    
    this.chosenOutputLabel.setBounds(110, 40, 150, 20);
    add(this.chosenOutputLabel);
    
    this.choose.setBounds(300, 40, 30, 20);
    add(this.choose);
    
    this.toggle.setBounds(125, 70, 100, 20);
    this.toggle.setEnabled(false);
    add(this.toggle);
    
    setSize(350, 130);
  } 
  
  public void toggleRunningState() {
    this.running = Boolean.valueOf(!this.running.booleanValue());
    this.toggle.setText(this.running.booleanValue() ? this.STOP_TEXT : this.RUN_TEXT);
  } 
  
  public void actionPerformed(ActionEvent e) {
    System.out.println(e.getActionCommand());
    if ("toggle".equals(e.getActionCommand())) {
      startRequestMgr();
    } else if ("chooseDir".equals(e.getActionCommand())) {
      handleStartOutputDirChoice();
    } 
  } 
  
  private void startRequestMgr() {
    if (this.htField.getText().equals("")) {
      this.htField.setText(this.DEFAULT_TAG);
    } 
    if (this.running.booleanValue()) {
      System.out.println("Was running. Trying to cancel");
      if (this.reqMgr != null) {
        this.reqMgr.end();
      } else {
        System.out.println("req mgr is null");
      } 
    } else {
      this.reqMgr = new RequestMgr(this.htField.getText(), this.outputDir, getPrevDownloaded(this.outputDir));
      this.reqMgr.setDebug(Boolean.valueOf(this.debugChkBox.isSelected()));
      System.out.println("Was not running. Trying to start");
      this.reqMgr.start();
    } 
    toggleRunningState();
  } 
  
  private ArrayList<String> getPrevDownloaded(File outputDir) {
    File[] files = outputDir.listFiles(new JpegFilter());
    ArrayList results = new ArrayList();
    for (int i = 0; i < files.length; i++) {
      results.add(files[i].getName());
    } 
    return results;
  } 
  
  private void handleStartOutputDirChoice() {
    int result = this.fileChooser.showOpenDialog(this);
    if (result == 0) {
      this.toggle.setEnabled(true);
      this.outputDir = this.fileChooser.getSelectedFile();
      this.chosenOutputLabel.setText(this.outputDir.getPath());
    } 
  } 
} 
