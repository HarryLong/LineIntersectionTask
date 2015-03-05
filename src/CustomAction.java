import javax.swing.*;

/**
 * Created by harry on 2014/10/21.
 */
public abstract class CustomAction extends AbstractAction {
    @Override
    public String toString()
    {
        return String.valueOf(getValue(SHORT_DESCRIPTION));
    }
}
