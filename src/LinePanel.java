import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 * Created by harry on 2014/10/21.
 */
public class LinePanel extends JPanel implements MouseListener{
    public static final int CLICK_PADDING = 10;
    public static final String RUN_1_MSG = "Using your LEFT hand, please try and intersect all the lines at their centers.";
    public static final String RUN_2_MSG = "Using your RIGHT hand, please try and intersect all the lines at their centers.";
    public static final String COMPLETION_MSG = "The experiment is over. Thanks for your participation! :)";

    private List<Integer> _activeLineIndices;

    Helper.ExperimentData _experimentData;
    private MainWindow _caller;

    public int runIndex;

    LinePanel(MainWindow caller) {
        this._caller = caller;
        this.addMouseListener(this);
        this.runIndex = 0;
        _activeLineIndices = new ArrayList<Integer>();
        initLayout();
    }

    public void initLayout() {
        setBackground(Color.BLACK);
    }

    public void reset()
    {
        _activeLineIndices.clear();
        for(int i = 0; i < _experimentData.line_data.length; i++)
            _activeLineIndices.add(i);
        repaint();
    }

    public void checkForLineIntersections(Point clicked)
    {
        Iterator<Integer> lineIndexIt = _activeLineIndices.iterator();
        while(lineIndexIt.hasNext())
        {
            int line_index = lineIndexIt.next();
            Helper.Line l = _experimentData.line_data[line_index];
            int line_y = l.startPosition.y;

            if(line_y > clicked.y-CLICK_PADDING && line_y < clicked.y+CLICK_PADDING)
            {
                int line_start_x = l.startPosition.x;
                int line_end_x = l.startPosition.x + l.length;

                if(clicked.x >= line_start_x && clicked.x <= line_end_x) // We have a match!
                {
                    _experimentData.clicked_lengths[runIndex][line_index] = clicked.x - line_start_x;
                    lineIndexIt.remove();
                    break;
                }
            }
        }
        repaint();
        if(_activeLineIndices.isEmpty())
            finish();
    }

    public void start(Subject s)
    {
        runIndex = 0;
        this._experimentData = LineGenerator.generateExperimentData(s, getWidth(), getHeight());
        reset();
        JOptionPane.showMessageDialog(LinePanel.this, RUN_1_MSG,
                null, JOptionPane.INFORMATION_MESSAGE);
        repaint();
    }

    public void finish()
    {
        if(runIndex == 0) // Proceed to the incorrect hand
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runIndex = 1;
                    reset();
                    JOptionPane.showMessageDialog(LinePanel.this, RUN_2_MSG,
                            null, JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
        else
        {
            JOptionPane.showMessageDialog(LinePanel.this, COMPLETION_MSG,
                    null, JOptionPane.INFORMATION_MESSAGE);
            _caller.experimentComplete(_experimentData);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
            g.setColor(Color.WHITE);
            for(int activeIndex : _activeLineIndices)
            {
                Helper.Line l = _experimentData.line_data[activeIndex];
                g.drawLine(l.startPosition.x, l.startPosition.y, l.startPosition.x+l.length, l.startPosition.y);
            }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        checkForLineIntersections(e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
    }
}
