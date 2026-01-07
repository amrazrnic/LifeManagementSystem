package lifemanagement.ui;

import javax.swing.*;
import java.awt.*;

public class TemaUtil {

    public static void primijeniTemu(JPanel panel, String tema) {
        if (panel == null) return;

        Color boja;

        if (tema == null) tema = "plava";

        switch (tema.toLowerCase()) {
            case "zelena":
                boja = new Color(210, 245, 210);
                break;
            case "roza":
                boja = new Color(255, 220, 235);
                break;
            case "narandzasta":
                boja = new Color(255, 235, 200);
                break;
            case "tamna":
            case "dark":
                boja = new Color(45, 45, 45);
                break;
            case "plava":
            default:
                boja = new Color(210, 225, 255);
                break;
        }

        panel.setBackground(boja);
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                ((JPanel) c).setBackground(boja);
            }
        }
    }
}
