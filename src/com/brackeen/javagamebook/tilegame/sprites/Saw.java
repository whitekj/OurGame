package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/**
    Class for a saw obstacle.
*/
public class Saw extends Sprite {

	public Saw(Animation anim) {
		super(anim);
	}

    public Saw(Animation left, Animation right) {
    	super(right);
        this.left = left;
        this.right = right;
    }

    private Animation left;
    private Animation right;


}
