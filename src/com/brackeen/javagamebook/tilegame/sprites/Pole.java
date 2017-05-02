package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;
import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/** 
    Class for a saw pole
 */

public class Pole extends Sprite {
	
	public Pole(Animation anim) {
		super(anim);
	}
	
	@Override
	public Object clone() {
        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(
                new Object[] {(Animation)anim.clone()});
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
