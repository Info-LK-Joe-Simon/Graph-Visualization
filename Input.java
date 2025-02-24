import java.awt.*;
import java.awt.event.*;

public class Input implements KeyListener, FocusListener,
		MouseListener, MouseMotionListener, MouseWheelListener {
	private boolean[] keys = new boolean[65536];
	private boolean[] mouseButtons = new boolean[4];
	private int mouseX = 0;
	private int mouseY = 0;

	private int mouseWheel=0;
	public boolean inFocus=true;

	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		int code = e.getButton();
		if (code > 0 && code < mouseButtons.length)
			mouseButtons[code] = true;
	}

	public void mouseReleased(MouseEvent e) {
		int code = e.getButton();
		if (code > 0 && code < mouseButtons.length)
			mouseButtons[code] = false;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		mouseWheel=e.getUnitsToScroll();
	}

	public void focusGained(FocusEvent e) {
		inFocus=true;
	}

	public void focusLost(FocusEvent e) {
		inFocus=false;
		for (int i = 0; i < keys.length; i++)
			keys[i] = false;
		for (int i = 0; i < mouseButtons.length; i++)
			mouseButtons[i] = false;
	}
	
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (code > 0 && code < keys.length)
			keys[code] = true;
	}

	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		if (code > 0 && code < keys.length)
			keys[code] = false;
	}
	
	public void releaseKey(KeyEvent e) {
		int code = e.getKeyCode();
		if (code > 0 && code < keys.length)
			keys[code] = false;
	}

	public void keyTyped(KeyEvent e) {
	}


	public boolean getKey(int key) {
		return keys[key];
	}


	public boolean getMouse(int button) {
		return mouseButtons[button];
	}


	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}
	
	public int getMouseXOnScreen() {
		return (int) MouseInfo.getPointerInfo().getLocation().getX();
	}

	public int getMouseYOnScreen() {
		return (int) MouseInfo.getPointerInfo().getLocation().getY();
	}

	public int getMouseWheel(){
		return mouseWheel;
	}
}