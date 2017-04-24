package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.88f;
    private static final float WALLJUMP_SPEED = -.90f;    
    private static final float DOUBLEJUMP_SPEED = -.66f;
    private static final float CLING_SPEED = .01f;

    private boolean onGround;
    private boolean onWall; //True if player is holding against wall
    private boolean canWallJump; //True if ability is unlocked
    private boolean canDoubleJump; //True if ability is unlocked
    private boolean doubleJumped; //True if player already double jumped

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight) {
        super(left, right, deadLeft, deadRight);
        doubleJumped = false;
        canWallJump = true; //Change to false later
        canDoubleJump = true; //Change to false later
    }

    @Override
    public void collideHorizontal() {
        setVelocityX(0);
        //Walljump mechanics
        if (!onGround && getVelocityY() >= 0) {
        	onWall = true;
        	setVelocityY(CLING_SPEED);
        }
    }

    @Override
    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
            onWall = false;
        }
        setVelocityY(0);
    }

    @Override
    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }

    public void wakeUp() {
        // do nothing
    }

    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
    	if (onGround || forceJump) {
    		doubleJumped = false;
    		onGround = false;
			setVelocityY(JUMP_SPEED);
    	}
    	else if (onWall && canWallJump) {
			onWall = false;
			setVelocityY(WALLJUMP_SPEED);
		}
    	else if (canDoubleJump && !doubleJumped && !onWall) {
    		setVelocityY(DOUBLEJUMP_SPEED);
    		doubleJumped = true;
    	}
    }
    
    public void duck(boolean shouldDuck) {
    	if (shouldDuck) {
    		//Change sprite
    	}
    	else {
    		//Change sprite back
    	}
    }
    
    @Override
    public void setX(float x) {
    	onWall = false;
        super.setX(x);
    }

    public float getMaxSpeed() {
        return 0.5f;
    }
    
    public void setOnWall(boolean b) {
    	onWall = b;
    }
    
    public void setCanWallJump(boolean b) {
    	canWallJump = b;
    }
    
    public void setCanDoubleJump(boolean b) {
    	canDoubleJump = b;
    }
}
