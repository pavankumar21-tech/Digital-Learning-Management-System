package com.dlms.main;
import com.dlms.view.LoginFrame;

public class Main {

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(()->{new LoginFrame().setVisible(true);
		});
	}

}
