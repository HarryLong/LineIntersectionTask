import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by harry on 2014/10/21.
 */
public class LineGenerator
{
    public static final int VERTICAL_PADDING = 20;
    public static final int HORIZONTAL_PADDING = 10;
    private static final int[] POSSIBLE_LENGTHS = new int[]{
            100,120,140,160,180,200,220,240,260
    };

    public static Helper.ExperimentData generateExperimentData(Subject s, int width, int height)
    {
        // First generate the lengths based on the scaling factor
        Helper.ExperimentData experimentData = new Helper.ExperimentData(s);
        int[] length_pool = Arrays.copyOf(POSSIBLE_LENGTHS, POSSIBLE_LENGTHS.length);
        for(int i = 0; i < length_pool.length; i++)
        {
            length_pool[i] = ScalingManager.toPixels(length_pool[i]/100.f);
        }

        int[] usedIndicesCount = new int[length_pool.length]; // Initialised to 0
        List<Integer> remainingIndices = new ArrayList<Integer>();
        for(int i = 0; i < length_pool.length; i++)
        {
            remainingIndices.add(i);
        }

        // divide the page in sections of equal width
        int lineCount = length_pool.length*2;
        int lineSectionWidth = height/lineCount;

        // Generate lines
        int variableWidth = lineSectionWidth-VERTICAL_PADDING;
        List<Helper.Line> generatedLines = new ArrayList<Helper.Line>();
        for(int i = 0; i < lineCount; i++)
        {
            Helper.Line line = new Helper.Line();
            // First select a random index
            int randomIndex = remainingIndices.get((int)(Math.random()*remainingIndices.size()));

            // Set the length
            line.length = length_pool[randomIndex];

            // Generate random x offset
            line.startPosition.x = Math.max(HORIZONTAL_PADDING, HORIZONTAL_PADDING +
                    ((int) (Math.random() * (width - HORIZONTAL_PADDING - line.length))));

            // Generate the random y offset
            line.startPosition.y = (i*lineSectionWidth) + VERTICAL_PADDING + ((int) (Math.random() * variableWidth));

            // Remove from selection pool
            if(++usedIndicesCount[randomIndex] == 2)
                remainingIndices.remove(new Integer(randomIndex));

            experimentData.line_data[i] = line;
        }

        return experimentData;
    }

    public static void main(String[] args)
    {
        int i = 13;
        System.out.println(13/2.f);
    }
}
