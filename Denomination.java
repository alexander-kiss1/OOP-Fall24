// Alex Kiss
// OOP Lab 1 Making Change
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;

// My Denomination class - record
record Denomination(String name, double amt, String form, String img) {
}

// My Purse class
class Purse {
    private final Map<Denomination, Integer> cash = new HashMap<>();

    public void add(Denomination type, int num) {
        cash.merge(type, num, Integer::sum);
    }

    public double remove(Denomination type, int num) {
        if (!cash.containsKey(type) || cash.get(type) < num) {
            throw new IllegalArgumentException("Not enough of the denomination.");
        }
        cash.put(type, cash.get(type) - num);
        return type.amt() * num;
    }

    public double getValue() {
        return cash.entrySet().stream()
                .mapToDouble(e -> e.getKey().amt() * e.getValue())
                .sum();
    }

    // creates a copy of cash ensures encapsulation allows to be passed
    public Map<Denomination, Integer> getCash() {
        return new HashMap<>(cash);
    }

    public String toString() {
        return cash.entrySet().stream()
                .map(e -> e.getKey().name() + ": " + e.getValue())
                .collect(Collectors.joining(", ", "Purse [", "]"));
    }
}

// My Register class
class Register {
    private static final List<Denomination> DENOMINATIONS = List.of(
            new Denomination("Hundred Bill", 100.0, "bill", "images/hundred_note.png"),
            new Denomination("Fifty Bill", 50.0, "bill", "images/fifty_note.png"),
            new Denomination("Twenty Bill", 20.0, "bill", "images/twenty_note.png"),
            new Denomination("Ten Bill", 10.0, "bill", "images/ten_note.png"),
            new Denomination("Five Bill", 5.0, "bill", "images/five_note.png"),
            new Denomination("One Bill", 1.0, "bill", "images/one_note.png"),
            new Denomination("Quarter", 0.25, "coin", "images/quarter.png"),
            new Denomination("Dime", 0.10, "coin", "images/dime.png"),
            new Denomination("Nickel", 0.05, "coin", "images/nickel.png"),
            new Denomination("Penny", 0.01, "coin", "images/penny.png")
    );

    public Purse makeChange(double amt) {
        Purse purse = new Purse();
        for (Denomination denom : DENOMINATIONS) {
            int num = (int) (amt / denom.amt());
            if (num > 0) {
                purse.add(denom, num);
                amt -= denom.amt() * num;
            }
            if (amt <= 0) break;
        }
        return purse;
    }

    public static List<Denomination> getDenominations() {
        return DENOMINATIONS;
    }
}

// My PursePanel class
class PursePanel extends JPanel {
    private Purse purse;
    private final Map<Denomination, Image> images;

    public PursePanel() {
        purse = new Purse();
        images = new HashMap<>();
        loadImages();
    }

    public void setPurse(Purse purse) {
        this.purse = purse;
    }

    private void loadImages() {
        // directory to my images with all the money
        String imagesDirPath = "/Users/alexkiss/Documents/OOP-24/Lab1OOP/src/";

        for (Denomination denom : Register.getDenominations()) {
            try {
                // gives file pathway
                File imgFile = new File(imagesDirPath, denom.img());
                if (imgFile.exists()) {
                    // Loads the images and reads them
                    Image img = ImageIO.read(imgFile);
                    images.put(denom, img);

                    // displays error message if cannot find path
                } else {
                    System.err.println("Image file not found: " + imgFile.getAbsolutePath());
                }

                // tells me the error message if cannot load the image
            } catch (IOException e) {
                System.err.println("Error loading image for " + denom.name() + ": " + e.getMessage());
            }
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (purse == null || purse.getCash().isEmpty()) {
            return;
        }

        // Dimensions for image
        int x = 10;
        int y = 20;
        int imageWidth = 50;
        int imageHeight = 50;

        for (Map.Entry<Denomination, Integer> entry : purse.getCash().entrySet()) {
            Denomination denom = entry.getKey();
            int count = entry.getValue();
            Image img = images.get(denom);

            if (img != null) {
                g.drawImage(img, x, y, imageWidth, imageHeight, this);
                y += imageHeight + 5;
            }
            // Formats my numbers as currency values
            g.drawString(denom.name() + ": " + count + " (" + String.format("$%.2f", denom.amt() * count) + ")", x, y);
            y += 15;
        }
    }
}

// My RegisterPanel class
class RegisterPanel extends JPanel {
    private final Register register;
    private final JTextField input;
    private final PursePanel changePanel;

    public RegisterPanel() {
        register = new Register();
        input = new JTextField(15);
        changePanel = new PursePanel();

        // Set up the layout
        setLayout(new BorderLayout());

        // Creates an input panel
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter amount: $"));
        inputPanel.add(input);

        // Adds the input panel to the RegisterPanel
        add(inputPanel, BorderLayout.NORTH);

        // Adds the change panel to the RegisterPanel
        add(changePanel, BorderLayout.CENTER);

        // Creates and adds the ActionListener for the input
        input.addActionListener(new InputListener());
    }

    private class InputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                double amount = Double.parseDouble(input.getText());
                Purse purse = register.makeChange(amount);
                changePanel.setPurse(purse);
                changePanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(RegisterPanel.this, "Invalid amount. Please enter a real value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// My MakingChange class
class MakingChange {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Making Change");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        RegisterPanel registerPanel = new RegisterPanel();
        frame.add(registerPanel);

        frame.setVisible(true);
    }
}
