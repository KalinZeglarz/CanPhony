package pl.poznan.put.windows

import pl.poznan.put.ClientConfig

import javax.swing.*

abstract class Window {

    protected ClientConfig config
    protected JFrame frame = null

    Window(ClientConfig config) {
        this.config = config
    }

    /*
    based on: https://stackoverflow.com/questions/21658144/jpanel-components-change-position-automatically
    */

    static JPanel getTwoColumnLayout(JLabel[] labels, JComponent[] fields) {
        if (labels.length != fields.length) {
            String s = labels.length + " labels supplied for "
            +fields.length + " fields!"
            throw new IllegalArgumentException(s)
        }
        JPanel panel = new JPanel()
        GroupLayout layout = new GroupLayout(panel)
        panel.setLayout(layout)
        layout.setAutoCreateGaps(true)
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup()
        GroupLayout.Group yLabelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
        hGroup.addGroup(yLabelGroup)
        GroupLayout.Group yFieldGroup = layout.createParallelGroup()
        hGroup.addGroup(yFieldGroup)
        layout.setHorizontalGroup(hGroup)
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup()
        layout.setVerticalGroup(vGroup)

        int p = GroupLayout.PREFERRED_SIZE
        for (JLabel label : labels) {
            yLabelGroup.addComponent(label)
        }
        for (JComponent field : fields) {
            yFieldGroup.addComponent(field, p, p, p)
        }
        for (int ii = 0; ii < labels.length; ii++) {
            vGroup.addGroup(layout.createParallelGroup().
                    addComponent(labels[ii]).
                    addComponent(fields[ii], p, p, p))
        }

        return panel
    }

    static JPanel getTwoColumnLayout(String[] labelStrings, JComponent[] fields) {
        List<JLabel> labels = []
        for (String labelString in labelStrings) {
            labels.add(new JLabel(labelString))
        }
        return getTwoColumnLayout(labels.toArray() as JLabel[], fields)
    }

    void create(JFrame frame) {
        this.frame = frame
    }

}
