package CSE621; 

import lejos.hardware.Button;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class CSE621 {
	public static void main(String[] args) {
		System.out.println("Starting...");
		Robot robot = Robot.getInstance(State.IDLE);
		while (true) {
			switch (robot.getState()) {

				case IDLE:
					if (Button.readButtons() == Button.ID_ENTER) {
						robot.setState(State.FORWARD);
					}
					break;

				case FORWARD:
					if (robot.getDistanceMetersFromObstacle() < 0.2) {
						robot.setState(State.BACKWARD);
					}
					if (robot.getColor() == Color.BLUE) {
						robot.setState(State.ROTATE_LEFT);
					}
					if (robot.getColor() == Color.RED) {
						robot.setState(State.IDLE);
					}
					break;

				case BACKWARD:
					if (System.currentTimeMillis() - robot.getStartTimeOfCurrentStateMillis() > 500) {
						robot.setState(State.ROTATE_RIGHT);
					}
					break;

				case ROTATE_LEFT:
					robot.setState(State.FORWARD);
					break;

				case ROTATE_RIGHT:
					robot.setState(State.FORWARD);
					break;
			}
		}
	}
}

class Robot {
	private UnregulatedMotor motorLeft;
	private UnregulatedMotor motorRight;
	private EV3UltrasonicSensor ultrasonicSensor;
	private EV3ColorSensor colorSensor;
	private EV3GyroSensor gyroSensor;

	private State state;
	private long startTimeOfCurrentStateMillis;

	private static Robot instance;

	// SINGLETON
	private Robot(State state) {
		this.motorLeft = new UnregulatedMotor(MotorPort.A);
		this.motorRight = new UnregulatedMotor(MotorPort.B);
		this.ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S1);
		this.colorSensor = new EV3ColorSensor(SensorPort.S2);
		this.gyroSensor = new EV3GyroSensor(SensorPort.S3);

		this.setPower(80);
		this.setState(state);

	}

	public static Robot getInstance(State state) {
		if (instance == null) {
			instance = new Robot(state);
		}
		instance.setState(state);
		return instance;
	}

	// METHODS
	public void goForward() {
		this.motorLeft.forward();
		this.motorRight.forward();
	}

	public void goBackward() {
		this.motorLeft.backward();
		this.motorRight.backward();
	}

	public float getCurrentAngle() {
		this.gyroSensor.setCurrentMode("Angle");
		float[] sample = { 0 };
		this.gyroSensor.fetchSample(sample, 0);
		return sample[0];
	}

	/*
	 * if degree is positive, rotate counterclockwise, else rotate clockwise
	 */
	public void rotateGivenDegrees(float degree) {
		float currentAngle = this.getCurrentAngle();

		if (degree == 0) {
			return;
		} else {
			float targetAngle = currentAngle + degree;
			if (degree > 0) {
				this.rotateCounterClockwise();
				// continue rotating until target angle is achieved
				while (this.getCurrentAngle() < targetAngle) {
				}
			} else {
				this.rotateClockwise();
				// continue rotating until target angle is achieved
				while (this.getCurrentAngle() > targetAngle) {
				}
			}
			this.stop(); // stop the robot after rotation
		}
	}

	public void rotateClockwiseGivenDegrees(float degree) {
		this.rotateGivenDegrees(-degree);
	}

	public void rotateCounterClockwiseGivenDegrees(float degree) {
		this.rotateGivenDegrees(degree);
	}

	public void rotateClockwise() {
		this.motorLeft.forward();
		this.motorRight.backward();
	}

	public void rotateCounterClockwise() {
		this.motorLeft.backward();
		this.motorRight.forward();
	}

	public void stop() {
		this.motorLeft.stop();
		this.motorRight.stop();
	}

	public float getDistanceMetersFromObstacle() {
		// current mode must be set before enabling
		this.ultrasonicSensor.setCurrentMode("Distance");
		this.ultrasonicSensor.enable();

		float[] sample = { 0 };
		this.ultrasonicSensor.getDistanceMode().fetchSample(sample, 0);
		return sample[0];
	}

	/*
	 * Return true if robot is currently over blue color, else return false
	 */
	public Color getColor() {
		this.colorSensor.setCurrentMode("ColorID");
		int colorId = this.colorSensor.getColorID();

		if (colorId == 0) {
			return Color.RED;
		} else if (colorId == 1) {
			return Color.BLUE;
		} else if (colorId == 2) {
			return Color.GREEN;
		} else if (colorId == 3) {
			return Color.YELLOW;
		} else {
			return Color.UNKNOWN;
		}
	}

	// GETTERS / SETTERS
	public long getStartTimeOfCurrentStateMillis() {
		return this.startTimeOfCurrentStateMillis;
	}

	public State getState() {
		return this.state;
	}

	/**
	 * This method sets the current state of the robot to the passed state, and also
	 * does any actions that need to be done upon entering that state
	 * 
	 * @param state
	 */
	public void setState(State state) {
		this.state = state;
		this.startTimeOfCurrentStateMillis = System.currentTimeMillis();
		System.out.println(state); 
		switch (state) {
		case IDLE:
			this.stop();
			break;
		case FORWARD:
			this.goForward();
			break;
		case BACKWARD:
			this.goBackward();
			break;
		case ROTATE_LEFT:
			this.rotateCounterClockwiseGivenDegrees(90);
			break;
		case ROTATE_RIGHT:
			this.rotateClockwiseGivenDegrees(90);
			break;
		}
	}

	private void setPower(int power) {
		this.motorLeft.setPower(power);
		this.motorRight.setPower(power);
	}

}

enum Color {
	RED, GREEN, BLUE, YELLOW, UNKNOWN
}

enum State {
	IDLE, FORWARD, BACKWARD, ROTATE_LEFT, ROTATE_RIGHT
}