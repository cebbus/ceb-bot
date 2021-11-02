package com.cebbus.chart.panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Objects;

public class WaitDialog {

    private final JDialog dialog;
    private final JButton cancelBtn;

    public WaitDialog() {
        Box box = Box.createVerticalBox();

        JLabel info = new JLabel("Please wait, while the system is processing...");
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));
        box.add(info);

        Box progressBox = Box.createHorizontalBox();
        progressBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBox.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBox.add(progressBar);

        JButton btn;
        try {
            URL resource = WaitDialog.class.getClassLoader().getResource("images/close-1.png");
            Objects.requireNonNull(resource);

            btn = new JButton(new ImageIcon(ImageIO.read(resource)));
        } catch (Exception e) {
            btn = new JButton("x");
        }

        this.cancelBtn = btn;
        this.cancelBtn.setVisible(false);
        this.cancelBtn.setMaximumSize(new Dimension(20, 14));
        this.cancelBtn.setPreferredSize(new Dimension(20, 14));
        progressBox.add(Box.createHorizontalStrut(2));
        progressBox.add(this.cancelBtn);

        box.add(progressBox);

        Frame rootFrame = JOptionPane.getRootFrame();

        this.dialog = new JDialog(rootFrame, true);
        this.dialog.setTitle("Please Wait");
        this.dialog.setLayout(new BorderLayout());
        this.dialog.setPreferredSize(new Dimension(300, 90));
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.dialog.setLocationRelativeTo(rootFrame);
        this.dialog.setAlwaysOnTop(true);
        this.dialog.setResizable(false);
        this.dialog.add(box, BorderLayout.CENTER);
    }

    public WaitDialog(ActionListener listener) {
        this();

        this.cancelBtn.setVisible(true);
        this.cancelBtn.addActionListener(e -> {
            listener.actionPerformed(e);
            hide();
        });
    }

    public void show() {
        this.dialog.pack();
        this.dialog.setVisible(true);
    }

    public void hide() {
        this.dialog.dispose();
    }
}
