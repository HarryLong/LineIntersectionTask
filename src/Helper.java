import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Created by harry on 2014/10/21.
 */
public class Helper
{
    public static class Line{
        Point startPosition;
        int length;

        Line()
        {
            startPosition = new Point();
            length = 0;
        }

        Line(Line l)
        {
            startPosition = new Point(l.startPosition);
            length = l.length;
        }
    }

    public static class ResultData{
        int[] line_lengths;
        int[][] clicked_lengths;

        ResultData()
        {
            line_lengths = new int[18];
            clicked_lengths = new int[2][18];
        }
    }

    public static class ExperimentData{
        Line[] line_data;
        int[][] clicked_lengths;
        Subject subject;

        ExperimentData(Subject s)
        {
            this.subject = s;
            line_data = new Line[18];
            clicked_lengths = new int[2][18];
        }
    }
    
    enum Hand {
    	Left,
    	Right
    }
}
