import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by harry on 2014/10/22.
 */
public class FormPanel extends JPanel{

    JTextField _participant_id_tf;

    RadioButtonEncapsulator _handedness_btns;
    RadioButtonEncapsulator _glasses_btns;
    RadioButtonEncapsulator glasses_on_btns;


    FormPanel() {
        initLayout();
        reset();
    }

    public void initLayout() {
        _participant_id_tf = new JTextField();
        _handedness_btns = new RadioButtonEncapsulator("Left", "Right");
        _glasses_btns = new RadioButtonEncapsulator("Yes", "No");
        _glasses_btns.radio_button_1.addActionListener(myGlassesStatusChangeTriggerAction);
        _glasses_btns.radio_button_2.addActionListener(myGlassesStatusChangeTriggerAction);

        glasses_on_btns = new RadioButtonEncapsulator("Yes", "No");

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        float column_1_weight = 0.5f;
        float column_2_3_weight = 0.25f;

        c.fill = GridBagConstraints.HORIZONTAL;

        // The Particpant ID
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5,5,5,5);


        {
            c.weightx = column_1_weight;
            JLabel lbl = new JLabel("Participant ID: ");
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl,c);
            c.gridx++;
            c.gridwidth = 2;
            c.weightx = column_2_3_weight;
            add(_participant_id_tf, c);
            c.gridwidth = 1;
        }


        // The handedness
        {
            c.weightx = column_1_weight;
            c.gridx = 0;
            c.gridy++;
            JLabel lbl = new JLabel("Handedness: ");
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl, c);
            c.gridx++;
            c.weightx = column_2_3_weight;
            add(_handedness_btns.radio_button_1, c);
            c.gridx++;
            add(_handedness_btns.radio_button_2, c);
        }

        // The glasses
        {
            c.weightx = column_1_weight;
            c.gridx = 0;
            c.gridy++;
            JLabel lbl = new JLabel("Do you have glasses?");
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl, c);
            c.gridx++;
            c.weightx = column_2_3_weight;
            add(_glasses_btns.radio_button_1, c);
            c.gridx++;
            c.weightx = column_2_3_weight;
            add(_glasses_btns.radio_button_2, c);
        }

        // The glasses on
        {
            c.weightx = column_1_weight;
            c.gridx = 0;
            c.gridy++;
            JLabel lbl = new JLabel("Are you wearing them now? ");
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lbl, c);
            c.weightx = column_2_3_weight;
            c.gridx++;
            add(glasses_on_btns.radio_button_1, c);
            c.gridx++;
            add(glasses_on_btns.radio_button_2, c);
        }
    }

    void refreshGlassesOnButtonGroup()
    {
        glasses_on_btns.setEnabled(_glasses_btns.radio_button_1.isSelected());
    }

    void reset()
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                _participant_id_tf.setText("");
                _handedness_btns.radio_button_2.setSelected(true);
                _glasses_btns.radio_button_2.setSelected(true);
                glasses_on_btns.radio_button_2.setSelected(true);
                refreshGlassesOnButtonGroup();
            }
        });
    }

    Helper.Hand getHandedness()
    {
        return _handedness_btns.radio_button_1.isSelected() ? Helper.Hand.Left : Helper.Hand.Right;
    }

    boolean hasGlasses()
    {
        return _glasses_btns.radio_button_1.isSelected();
    }

    boolean isWearingGlasses()
    {
        return glasses_on_btns.radio_button_1.isSelected();
    }

    class RadioButtonEncapsulator
    {
        ButtonGroup button_group;
        JRadioButton radio_button_1;
        JRadioButton radio_button_2;

        RadioButtonEncapsulator(String btn_1_name, String btn_2_name)
        {
            button_group = new ButtonGroup();
            radio_button_1 = new JRadioButton(btn_1_name);
            radio_button_2 = new JRadioButton(btn_2_name);
            button_group.add(radio_button_1);
            button_group.add(radio_button_2);
        }

        void setEnabled(final boolean enabled)
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    radio_button_1.setEnabled(enabled);
                    radio_button_2.setEnabled(enabled);
                }
            });
        }
    }

    // Actions
    GlassesStatusChangeTrigger myGlassesStatusChangeTriggerAction = new GlassesStatusChangeTrigger();
    class GlassesStatusChangeTrigger extends CustomAction {
        GlassesStatusChangeTrigger() {
            putValue(SHORT_DESCRIPTION, "Glasses status changed");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshGlassesOnButtonGroup();
        }
    }

}
