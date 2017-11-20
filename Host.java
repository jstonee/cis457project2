import java.lang.*;
import javax.swing.*;

public final class Host {

	private static JFrame frame;

    public static void main(String args[]) {
    	// Open HostGUI
		frame = new JFrame("GV-NAPSTER Host");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		HostGUI panel = new HostGUI();
		frame.getContentPane().add(panel);

		frame.pack();
		frame.setVisible(true);
    }
}
