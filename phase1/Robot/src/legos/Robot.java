//author: Bhisma Adhikari, Nick DeGennaro
//instructor: Dr. Eric Rapos
//course: CSE 621 
//file content: Robot class 

package legos;

import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.utility.Delay;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;

public class Robot {
	private UnregulatedMotor motorLeft;
	private UnregulatedMotor motorRight;
	private EV3UltrasonicSensor ultrasonicSensor;
	private EV3ColorSensor colorSensor;
	private EV3GyroSensor gyroSensor;

	// SINGLETON DESIGN PATTERN
	private Robot() {
		this.motorLeft = new UnregulatedMotor(MotorPort.A);
		this.motorRight = new UnregulatedMotor(MotorPort.D);
		this.ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S1);
		this.colorSensor = new EV3ColorSensor(SensorPort.S4);
		this.gyroSensor = new EV3GyroSensor(SensorPort.S2);
	}

	private static Robot instance = new Robot();

	public static Robot getInstance() {
		return instance;
	}

	// METHODS
	private void goForwardLeftWheel(int power) {
		this.motorLeft.backward();
		this.motorLeft.setPower(power);
	}

	private void goForwardRightWheel(int power) {
		this.motorRight.backward();
		this.motorRight.setPower(power);
	}

	private void goBackwardLeftWheel(int power) {
		this.motorLeft.forward();
		this.motorLeft.setPower(power);
	}

	private void goBackwardRightWheel(int power) {
		this.motorRight.forward();
		this.motorRight.setPower(power);
	}

	public float getCurrentAngle() {
		this.gyroSensor.setCurrentMode("Angle");
		float[] sample = { 0 };
		this.gyroSensor.fetchSample(sample, 0);
		return sample[0];
	}

	// todo: implement angle error correction
	public void goForward(int power) {
		this.goForwardLeftWheel(power);
		this.goForwardRightWheel(power);
	}

	public void goBackward(int power) {
		this.goBackwardLeftWheel(power);
		this.goBackwardRightWheel(power);
	}

	/*
	 * if degree is positive, rotate counterclockwise, else rotate clockwise
	 */
	public void rotateGivenDegrees(int power, float degree) {
		float currentAngle = this.getCurrentAngle();

		if (degree == 0) {
			return;
		} else {
			float targetAngle = currentAngle + degree;
			if (degree > 0) {
				this.rotateCounterClockwise(power);
				// continue rotating until target angle is achieved
				while (this.getCurrentAngle() < targetAngle) {
				}
			} else {
				this.rotateClockwise(power);
				// continue rotating until target angle is achieved
				while (this.getCurrentAngle() > targetAngle) {
				}
			}
			this.stop(); // stop the robot after rotation
		}
	}

	public void rotateClockwiseGivenDegrees(int power, float degree) {
		this.rotateGivenDegrees(power, -degree);
	}

	public void rotateCounterClockwiseGivenDegrees(int power, float degree) {
		this.rotateGivenDegrees(power, degree);
	}

	public void rotateClockwise(int power) {
		this.goForwardLeftWheel(power);
		this.goBackwardRightWheel(power);
	}

	public void rotateCounterClockwise(int power) {
		this.goForwardRightWheel(power);
		this.goBackwardLeftWheel(power);
	}

	public void stop() {
		this.motorLeft.stop();
		this.motorRight.stop();
	}

	public boolean obstacleAhead(double distance) {
		// current mode must be set before enabling
		this.ultrasonicSensor.setCurrentMode("Distance");
		this.ultrasonicSensor.enable();

		float[] sample = { 0 };
		this.ultrasonicSensor.getDistanceMode().fetchSample(sample, 0);
		if (sample[0] <= distance) {
			return true;
		}
		return false;

	}

	/*
	 * Return true if robot is currently over blue color, else return false
	 */
	public boolean overBlue() {
		this.colorSensor.setCurrentMode("ColorID");
		int colorId = this.colorSensor.getColorID();

		// 0: red
		// 1: green
		// 2: blue
		if (colorId == 2) {
			return true;
		}
		return false;
	}

}