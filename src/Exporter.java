import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by harry on 2014/10/22.
 */
public class Exporter {
    public static final String HEADING = buildHeading();


    public static void export(final FileWriter fw, final Map<Subject, Helper.ResultData> allData)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if(fw != null)
                    {
                        fw.write(HEADING);
                        fw.write(System.lineSeparator());

                        for(Map.Entry<Subject, Helper.ResultData> pair : allData.entrySet())
                        {
                            Subject s = pair.getKey();
                            Helper.ResultData subjectData = pair.getValue();

                            for(int runIndex = 0; runIndex < 2; runIndex++)
                            {
                                fw.write(s.name + ",");
                                fw.write((s.goodHand == Helper.Hand.Right ? "Right" : "Left")  + ",");
                                fw.write((s.hasGlasses ? "Yes" : "No")  + ",");
                                fw.write((s.hasGlasses && s.glassesOn ? "Yes" : "No")  + ",");
                                fw.write((Helper.Hand.values()[runIndex] == Helper.Hand.Left ? "Left" : "Right") + ",");
                                for(int lineIndex = 0; lineIndex < 18; lineIndex++)
                                {
                                    try {
                                        fw.write(subjectData.line_lengths[lineIndex] + ",");
                                        fw.write(subjectData.clicked_lengths[runIndex][lineIndex] + ",");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                fw.write(System.lineSeparator());
                            }
                        }
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String buildHeading()
    {
        String heading = "Subject_name,handedness,glasses,wearing glasses,hand";
        for(int i = 0; i < 18; i++)
            heading += ",line length, intersection length";

        return heading;
    }
}