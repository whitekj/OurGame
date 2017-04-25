package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/**
    The Player.
 */
public class Player extends Sprite {

	public Player(Animation left, Animation right, Animation deadLeft, Animation deadRight, 
			Animation duckingLeft, Animation duckingRight, Animation walkingLeft, Animation walkingRight) {
		super(right);
		this.left = left;
		this.right = right;
		this.deadLeft = deadLeft;
		this.deadRight = deadRight;
		this.duckingLeft = duckingLeft;
		this.duckingRight = duckingRight;
		this.walkingLeft = walkingLeft;
		this.walkingRight = walkingRight;
		ducking = false;
		doubleJumped = false;
		canWallJump = true; //Change to false later
		canDoubleJump = true; //Change to false later
	}

	public void collideHorizontal() {
		setVelocityX(0);
		//Wall jump mechanics
		if (!onGround && getVelocityY() >= 0) {
			onWall = true;
			setVelocityY(CLING_SPEED);
		}
	}

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
		ducking = false;
		if (anim == duckingLeft) {
			anim = left;
		}
		if (anim == duckingRight) {
			anim = right;
		}
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

	/**
	 * Sets if player is ducking, or sets to false if player is not on ground
	 */

	public void duck(boolean isDucking) {
		if (onGround) {
			ducking = isDucking;
		}
		else {
			ducking = false;
		}
	}

	/**
	 * Returns true if player is ducking.
	 */

	public boolean isDucking() {
		return ducking;
	}

	/**
	 * Returns true if player is alive.
	 */
	public boolean isAlive() {
		return (state == STATE_NORMAL);
	}
	
	/**
     * Returns false
    */
	public boolean isFlying() {
		return false;
	}


	/**
    * Gets the state of this Creature. The state is either
    * STATE_NORMAL, STATE_DYING, or STATE_DEAD.
	 */
	public int getState() {
		return state;
	}


	/**
    * Sets the state of this Creature to STATE_NORMAL,
    * STATE_DYING, or STATE_DEAD.
	 */
	public void setState(int state) {
		if (this.state != state) {
			this.state = state;
			stateTime = 0;
			if (state == STATE_DYING) {
				setVelocityX(0);
				setVelocityY(0);
			}
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

	@Override
	public Object clone() {
		// use reflection to create the correct subclass
		Constructor constructor = getClass().getConstructors()[0];
		try {
			return constructor.newInstance(new Object[] {
					(Animation)left.clone(),
					(Animation)right.clone(),
					(Animation)deadLeft.clone(),
					(Animation)deadRight.clone(),
					(Animation)duckingLeft.clone(),
					(Animation)duckingRight.clone(),
					(Animation)walkingLeft.clone(),
					(Animation)walkingRight.clone()
			});
		}
		catch (Exception ex) {
			// should never happen
			ex.printStackTrace();
			return null;
		}
	}


	public void update(long elapsedTime) {
		// select the correct Animation
		Animation newAnim = anim;
		if (getVelocityX() == 0) {
			if (newAnim == walkingLeft) {
				newAnim = left;
			}
			if (newAnim == walkingRight) {
				newAnim = right;
			}
		}
		if (getVelocityX() < 0) {
			if (onGround ) {
				newAnim = walkingLeft;
			}
			else {
				newAnim = left;
			}
		}
		else if (getVelocityX() > 0) {
			if (onGround ) {
				newAnim = walkingRight;
			}
			else {
				newAnim = right;
			}
		}
		if (state == STATE_DYING) {
			ducking = false;
			if (newAnim == left || newAnim == walkingLeft) {
				newAnim = deadLeft;
			}
			else if (newAnim == right  || newAnim == walkingRight) {
				newAnim = deadRight;
			}
		}
		else if (isDucking()) {
			if (newAnim == left || newAnim == walkingLeft) {
				newAnim = duckingLeft;
			}
			else if (newAnim == right  || newAnim == walkingRight) {
				newAnim = duckingRight;
			}
		}
		// update the Animation
		if (anim != newAnim) {
			anim = newAnim;
			anim.start();
		}
		else {
			anim.update(elapsedTime);
		}

		// update to "dead" state
		stateTime += elapsedTime;
		if (state == STATE_DYING && stateTime >= DIE_TIME) {
			setState(STATE_DEAD);
		}
	}

	private static final float JUMP_SPEED = -.90f;
	private static final float WALLJUMP_SPEED = -.80f;    
	private static final float DOUBLEJUMP_SPEED = -.60f;
	private static final float CLING_SPEED = .01f;
	private static final int DIE_TIME = 1000;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_DYING = 1;
	public static final int STATE_DEAD = 2;

	private boolean onGround;
	private boolean onWall; //True if player is holding against wall
	private boolean ducking;
	private boolean canWallJump; //True if ability is unlocked
	private boolean canDoubleJump; //True if ability is unlocked
	private boolean doubleJumped; //True if player already double jumped
	private int state;
	private long stateTime;
	private Animation left;
	private Animation right;
	private Animation deadLeft;
	private Animation deadRight;
	private Animation walkingLeft;
	private Animation walkingRight;
	private Animation duckingLeft;
	private Animation duckingRight;
}
