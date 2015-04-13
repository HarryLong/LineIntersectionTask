import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Map;

/**
 * Created by harry on 2014/10/21.
 */
public class MainWindow extends JFrame {
    public static final String TITLE = "Line Intersection Experiment";

    private ScalingManager m_scalingMgr;
    private LinePanel m_linePanel;
    private FormPanelWrapper m_formPanel;
    private DBManager m_dbManager;

    private JPanel cards;

    public MainWindow() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(900, 900));
        setLocationRelativeTo(null);

        m_scalingMgr = new ScalingManager(this);
        m_dbManager = new DBManager();

        initMenu();

        initLayout();
    }

    private void initLayout()
    {
        m_formPanel = new FormPanelWrapper();
        m_linePanel = new LinePanel(this);

        CardLayout cl = new CardLayout();
        cards = new JPanel(cl);
        cards.add(Card.Form.cardName, m_formPanel);
        cards.add(Card.Experiment.cardName, m_linePanel);

        add(cards);

        switchActiveCard(Card.Form);
    }

    enum Card {
        Form("Form"),
        Experiment("Experiment");

        Card(String name)
        {
            this.cardName = name;
        }
        String cardName;
    }
    private Card activeCard;
    private void switchActiveCard(Card card)
    {
        if(activeCard == null || activeCard != card)
        {
            CardLayout cl = (CardLayout) cards.getLayout();
            cl.show(cards, card.cardName);
            activeCard = card;
            repaint();
        }
    }

    private void initMenu()
    {
        JMenuBar menuBar = new JMenuBar();

        //**********************************************************************
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem(myExitAction);
        exitMenuItem.setText(myExitAction.toString());
//        exitMenuItem.setText(String.valueOf(myExitAction.getValue(Action.SHORT_DESCRIPTION)));
        fileMenu.add(exitMenuItem);

        //********SETTINGS MENU*************************************************
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem scalingManagerLauncherMI = new JMenuItem(myScalingManagerLauncherAction);
        scalingManagerLauncherMI.setText(String.valueOf(myScalingManagerLauncherAction.getValue(Action.SHORT_DESCRIPTION)));
        settingsMenu.add(scalingManagerLauncherMI);

        //********EXPORT MENU*************************************************
        JMenu exportMenu = new JMenu("Export");
        JMenuItem exportMI = new JMenuItem(myExportAction);
        exportMI.setText(String.valueOf(myExportAction.getValue(Action.SHORT_DESCRIPTION)));
        exportMenu.add(exportMI);

        // Scale Manager
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(exportMenu);

        setJMenuBar(menuBar);
    }

    public void showScalingManager()
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_scalingMgr.setVisible(true);
            }
        });
    }

    public void exit()
    {
        int ret = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", null, JOptionPane.YES_NO_OPTION);

        if(ret == JOptionPane.YES_OPTION)
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public void startExperiment(Subject s)
    {
        switchActiveCard(Card.Experiment);
        m_linePanel.start(s);
    }

    public void experimentComplete(Helper.ExperimentData experimentResults)
    {
//        Helper.ResultData resultData = new Helper.ResultData();

//        // Data conversions
//        for(int i = 0; i < experimentResults.line_data.length; i++)
//            resultData.line_lengths[i] = (int) Math.round(ScalingManager.toCm(experimentResults.line_data[i].length) * 100.f);
//        // Now the clicked lengths
//        for(int i = 0; i < 2; i++)
//        {
//            for(int k = 0; k < experimentResults.clicked_lengths[i].length; k++)
//            {
//                resultData.clicked_lengths[i][k] = (int) Math.round(ScalingManager.toCm(experimentResults.clicked_lengths[i][k]) * 100.f);
//            }
//        }

        // Add to db
        {
            final Helper.ExperimentData finalResultData = experimentResults;
//            final Subject finalSubject = experimentResults.subject;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Add subject
                    m_dbManager.addSubject(finalResultData.subject);
                    // Add the data
                    for(int i = 0; i < 2; i++)
                        m_dbManager.addData(finalResultData.subject.uniqueId, Helper.Hand.values()[i], finalResultData.line_data, finalResultData.clicked_lengths[i]);

                    m_formPanel.formPanel.reset();
                }
            });
        }

        switchActiveCard(Card.Form);
    }

    public FileWriter getCsvFileWriter() throws IOException
    {
        // First get the file in question
        JFileChooser fc = new JFileChooser();
        int ret = fc.showOpenDialog(this);
        if(ret != JFileChooser.APPROVE_OPTION)
            return null;

        File outputFile;
        String filePath = fc.getSelectedFile().getAbsolutePath();
        if(filePath.endsWith(".csv"))
            outputFile = fc.getSelectedFile();
        else
            outputFile = new File(filePath + ".csv");

        return new FileWriter(outputFile);
    }

    public void export()
    {
        FileWriter fw;
        try {
            if((fw = getCsvFileWriter()) != null)
            {
                Map<Subject, Helper.ResultData>  allData = m_dbManager.getAllResultData();
                Exporter.export(fw, allData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Custom Actions
    private ExitAction myExitAction = new ExitAction();
    private ScalingManagerLauncherAction myScalingManagerLauncherAction = new ScalingManagerLauncherAction();
    private ExportAction myExportAction = new ExportAction();

    class ExitAction extends CustomAction {
        ExitAction() {
            putValue(SHORT_DESCRIPTION, "Exit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_ESCAPE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    }

    class ScalingManagerLauncherAction extends CustomAction {
        ScalingManagerLauncherAction() {
            putValue(SHORT_DESCRIPTION, "Scaling Manager");
            putValue(MNEMONIC_KEY, KeyEvent.ALT_DOWN_MASK|KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showScalingManager();
        }
    }

    class ExportAction extends CustomAction {
        ExportAction() {
            putValue(SHORT_DESCRIPTION, "Export");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            export();
        }
    }

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    public class FormPanelWrapper extends JPanel
    {
        FormPanel formPanel;

        FormPanelWrapper()
        {
            initLayout();
        }

        void initLayout()
        {
            formPanel = new FormPanel();

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.NONE;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.insets = new Insets(20,0,0,0);

            c.gridx = 0;
            c.gridy = 0;
            add(formPanel, c);

            JButton startBtn = new JButton(new CustomAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String participantId;
                    if(!(participantId = formPanel._participant_id_tf.getText().trim()).isEmpty())
                    {
                        Subject s = new Subject(participantId, formPanel.getHandedness(), formPanel.hasGlasses(),
                                formPanel.isWearingGlasses(), null);
                        startExperiment(s);
//                        generateAndInsertDummyData(s);
                    }
                }
            });
            startBtn.setText("Start!");
            c.gridy++;
            add(startBtn, c);
        }
    }
}

