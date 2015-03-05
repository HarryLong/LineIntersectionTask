/**
 * Created by harry on 2014/10/21.
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.*;

public class ScalingManager extends JDialog implements MouseWheelListener, KeyEventDispatcher{

    public static final String TITLE = "Please set the bars exactly 1 cm apart";
    public static final int DEFAULT_SCALING_FACTOR = 35;

    public static int scaleFactor;

    private ScalePanel scalePnl;
    private boolean accelerateScroller;
    private int tmpScaleFactor;
    private ScaleFileManager scaleFileMgr;

    public static float toCm(int distanceInPixels)
    {
        return (distanceInPixels / (float) scaleFactor);
    }

    public static int toPixels(double distanceInCm)
    {
        return (int) (scaleFactor * distanceInCm);
    }

    public ScalingManager(JFrame parent)
    {
        super(parent, TITLE, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(false);
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dSize = new Dimension((int)screenSize.getWidth()/2, (int)screenSize.getHeight()/2);
        setSize(dSize);
        setLocationRelativeTo(null);

        scaleFileMgr = new ScaleFileManager();
        Integer loadedScaleFactor = null;
        if((loadedScaleFactor = scaleFileMgr.loadScalingFactorFromFile()) == null)
        {
            loadedScaleFactor = DEFAULT_SCALING_FACTOR;
            scaleFileMgr.writeScalingFactorToFile(DEFAULT_SCALING_FACTOR);
        }
        scaleFactor = tmpScaleFactor = loadedScaleFactor;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        addMouseWheelListener(this);
        initComponents();
        initLayout();
    }

    private void initComponents()
    {
        scalePnl = new ScalePanel();


    }

    private void initLayout()
    {
        JButton closeBtn = new JButton(myCancelAction);
        closeBtn.setText(myCancelAction.toString());

        JButton okBtn = new JButton(myConfirmAction);
        okBtn.setText(myConfirmAction.toString());

        Container contentPane = getContentPane();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        // Add the scale panel
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.95;
        contentPane.add(scalePnl,c);

        // Add the close btn
        c.fill = GridBagConstraints.NONE;
        c.gridy = 1;
        c.weightx = 0.2;
        c.weighty = 0.05;
        c.gridwidth = 1;
        contentPane.add(okBtn,c);
        c.gridx = 1;
        contentPane.add(closeBtn,c);
    }

    public int getScaleFactor()
    {
        return scaleFactor;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent ke) {
        if(ke.isControlDown())
        {
            accelerateScroller = true;
        }
        else
        {
            accelerateScroller = false;
        }

        return false; // Always dispatch to other components
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        System.out.println("Wheel scrolled");
        int count = mwe.getWheelRotation();
        if(accelerateScroller)
            count *= 10;

        tmpScaleFactor = Math.max(2,tmpScaleFactor+count);
        repaint();
    }

    private enum ActionCommands
    {
        OK("OK"), CANCEL("CANCEL");

        ActionCommands(String p_command_string)
        {
            this.command_string = p_command_string;
        }
        String command_string;
    }


    // Custom Actions
    private ConfirmAction myConfirmAction = new ConfirmAction();
    private CancelAction myCancelAction = new CancelAction();
    class ConfirmAction extends CustomAction {
        ConfirmAction() {
            putValue(SHORT_DESCRIPTION, "OK");
            putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            scaleFactor = tmpScaleFactor;
            scaleFileMgr.writeScalingFactorToFile(scaleFactor);
            dispose();
        }
    }

    class CancelAction extends CustomAction {
        CancelAction() {
            putValue(SHORT_DESCRIPTION, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_ESCAPE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tmpScaleFactor = scaleFactor; // reset scale factor
            dispose();
        }
    }

    @Override
    public void dispose()
    {
        tmpScaleFactor = scaleFactor; // Reset any unchanged modifications
        scaleFileMgr.writeScalingFactorToFile(scaleFactor); // Ensure file is up to date
        super.dispose();
    }

    private class ScalePanel extends JPanel{

        public ScalePanel()
        {
            initLayout();
        }

        public void initLayout()
        {
            setBackground(Color.BLACK);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.WHITE);
            int h = getHeight();
            int w = getWidth();

            // Draw horizontal line 1
            int y = (int) (h/4d);
            int x1 = (int) (w/4d);
            int x2 = 3*x1;
            g.drawLine(x1, y, x2, y);

            // Draw horizontal line 2
            y += tmpScaleFactor;
            g.drawLine(x1, y, x2, y);
        }
    }

    public class ScaleFileManager
    {
        public static final String SETTINGS_FILE = "settings.conf";
        private File settingsFile;

        public ScaleFileManager()
        {
            this.settingsFile = new File(SETTINGS_FILE);
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Failed to create settings file: " + SETTINGS_FILE);
                e.printStackTrace();
                System.exit(1);
            }
        }

        public Integer loadScalingFactorFromFile()
        {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(Paths.get(settingsFile.getAbsolutePath()), Charset.defaultCharset());
            } catch (IOException e) {
                System.err.println("Unable to load scaling factor from file!");
                e.printStackTrace();
            }
            if(lines != null && lines.size() > 0)
                return Integer.valueOf(lines.get(0));
            else
                return null;
        }

        public void writeScalingFactorToFile(int scaleFactor)
        {
            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(settingsFile));
                output.write(String.valueOf(scaleFactor));
                output.close();
            } catch ( IOException e ) {
                System.err.println("Unable to write to file: " + SETTINGS_FILE);
                e.printStackTrace();
            }
        }
    }
}
