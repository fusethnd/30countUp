import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedRectButton extends JButton {
    private final Color background;
    private final Color foreground;

    public RoundedRectButton(String label, Color background, Color foreground) {
        super(label);
        this.background = background;
        this.foreground = foreground;
        setOpaque(false);
        setContentAreaFilled(false);
        setForeground(foreground);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(background);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 48, 48); // radius 24
        g2.fill(round);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(foreground);
        Graphics2D g2 = (Graphics2D) g;
        Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 48, 48);
        g2.draw(round);
    }

    @Override
    public boolean contains(int x, int y) {
        Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 48, 48);
        return round.contains(x, y);
    }
}