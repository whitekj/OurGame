package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/**
   Class for a spike obstacle.
*/
public class Spike extends Sprite {
	
	public Spike(Animation anim) {
		super(anim);
	}

    public Spike(Animation left, Animation right) {
    	super(right);
        this.left = left;
        this.right = right;
    }

    private Animation left;
    private Animation right;


    
    
}
