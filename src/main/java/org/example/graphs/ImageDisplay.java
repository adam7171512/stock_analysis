package org.example.graphs;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageDisplay {

    public static void displayImageFromURL(String urlString) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a JFrame
                JFrame frame = new JFrame("Image Display");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Fetch the image from the URL
                URL url = new URL(urlString);
                Image image = ImageIO.read(url);

                // Display the image in a JLabel
                JLabel label = new JLabel(new ImageIcon(image));
                frame.getContentPane().add(label, BorderLayout.CENTER);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}