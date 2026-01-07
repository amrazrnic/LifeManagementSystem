package lifemanagement;

import lifemanagement.ui.AppProzor;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppProzor().setVisible(true));
    }
}

