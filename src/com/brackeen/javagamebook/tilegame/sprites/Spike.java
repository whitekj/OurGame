package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/**
   Class for a spike obstacle.
*/
public class Spike extends Sprite {
	
	public Spike(Animation up) {
		super(up);
	}
	
	public Spike(Animation up, Animation down, Animation left, Animation right) {
		super (up);
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public Object clone() {
        // use reflection to create the correct subclass
        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(
                new Object[] {(Animation)anim.clone()});
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
    }

    private Animation up;
    private Animation down;
    private Animation left;
    private Animation right;


    
    
}
