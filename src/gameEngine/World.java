package gameEngine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class World {
	/**
	 * Struct to store state of the "world" Walls, nibbles and global time
	 */
	public int height, width;
	public long clock;
	public int maxNibbles = 20;
	private Semaphore nibbleProtect = new Semaphore(1); // protect nibble list
														// add/remove with
														// semaphore
	private LinkedList<PhysicalCircle> nibbles = new LinkedList<PhysicalCircle>();

	public void newNibble(int n) {
		try {
			nibbleProtect.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < n; i++) {
			if (nibbles.size() >= maxNibbles)
				break;
			PhysicalCircle nibble = new PhysicalCircle(0, 0, GameLoop.globalCircleRadius);
			nibble.x = Math.random() * (width - 2 * nibble.rad) + nibble.rad;
			nibble.y = Math.random() * (height - 2 * nibble.rad) + nibble.rad;

			nibble.vx = 2 * (Math.random() - .5);
			nibble.vy = 2 * (Math.random() - .5);
			nibble.t = 0;
			nibbles.add(nibble);
		}
		nibbleProtect.release();
	}

	public LinkedList<PhysicalCircle> getNibbles() {
		return nibbles;
	}

	public int calcValue(PhysicalCircle p) {
		return (int) (5 + (8d * Math.min(Math.exp(-(double) (p.t - 800) / 2000d), 1)));
	}

	public void update(int w, int h) {
		this.width = w;
		this.height = h;
		for (PhysicalCircle p : nibbles) {
			p.updatePosition();
			p.collideWall(50, 50, w - 50, h - 50);
		}
		clock += GameLoop.UPDATEPERIOD;
	}

	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		for (PhysicalCircle nibble : nibbles) {
			double diameter = 2 * nibble.rad + 1;
			double x0 = nibble.x - nibble.rad;
			double y0 = nibble.y - nibble.rad;

			var circle = new Ellipse2D.Double(x0, y0, diameter, diameter);
			GradientPaint gp = new GradientPaint((float) (x0), (float) (y0), Color.RED, (float) (x0 + diameter),
					(float) (y0 + diameter), Color.BLACK, true);

			g2d.setPaint(gp);
			g2d.fill(circle);
		}
	}

	public void removeNibbles(LinkedList<PhysicalCircle> rem) {
		try {
			nibbleProtect.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (PhysicalCircle p : rem) {
			nibbles.remove(p);
		}
		nibbleProtect.release();
	}

	public void reset() {
		try {
			nibbleProtect.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nibbles.clear();
		nibbleProtect.release();
		clock = 0;
	}
}
