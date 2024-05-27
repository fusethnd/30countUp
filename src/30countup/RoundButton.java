import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RoundButton extends JButton {
    private Color background;
    private Color foreground;

    public RoundButton(String label, Color background, Color foreground) {
        super(label);
        this.background = background;
        this.foreground = foreground;
        setOpaque(false); // Make button non-opaque
        setContentAreaFilled(false);
        setForeground(foreground);
        setFocusPainted(false); // Remove focus border
        setBorderPainted(false); // Remove border if not necessary
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(background);
        g.fillOval(0, 0, getSize().width-1, getSize().height-1);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(foreground);
        g.drawOval(0, 0, getSize().width-1, getSize().height-1);
    }

    // Hit detection.
    @Override
    public boolean contains(int x, int y) {
        Shape shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        return shape.contains(x, y);
    }
}
