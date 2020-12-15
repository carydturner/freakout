import java.awt.*;
import acm.program.*;
import acm.graphics.*;



public class GFace extends GCompound {
	
	public GFace(double width, double height) {
		GOval head = new GOval(0, 0, width, height);
		GArc mouth = new GArc(width * 0.75, height * 0.15, 180, 180);
		GArc lense1 = new GArc(width * .35, height * .48, 180, 180);
		GArc lense2 = new GArc(width * .35, height * .48, 180, 180);
		GRect band = new GRect(width * .75, height * .04);
		lense1.setFilled(true);
		lense2.setFilled(true);
		band.setFilled(true);
		head.setFilled(true);
		head.setFillColor(Color.YELLOW);
		add(head);
		add(mouth, width * .15, height * .65);
		add(lense1, width * .13, height * .15);
		add(lense2, width * .52, height * .15);
		add(band, width * .12, height * .35);
	}
}
