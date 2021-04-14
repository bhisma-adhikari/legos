//author: Bhisma Adhikari, Nick DeGennaro 
//instructor: Dr. Eric Rapos
//course: CSE 621 
//file content: Main class to run demo of phase 1 of Legos project 

package legos;

import lejos.utility.Delay;

public class Main {
	public static void main(String args[]) {
		int power = 30;

		task1(power);
		task2(power);
		task3(power);
//		test(power); 
	}

	private static void test(int power) {
		System.out.println("test");
		Robot robot = Robot.getInstance();

	}

	/*
	 * Drive forward towards a wall and stop when it is ~6” away, then back up for 3
	 * seconds.
	 */
	private static void task1(int power) {
		System.out.println("Task 1");
		Robot robot = Robot.getInstance();
		double distanceMeters = 6 * 0.0254; // inches to meters conversion

		robot.goForward(power);
		// go forward until an obstacle is detected within 6 inches distance
		while (!robot.obstacleAhead(distanceMeters)) {
		}
		robot.goBackward(power);
		Delay.msDelay(3000); // back up for 3 seconds
		robot.stop();
	}

	/*
	 * Rotate 90 degrees to the left, stop, then rotate 180 degrees to the right.
	 */
	private static void task2(int power) {
		System.out.println("Task 2");
		Robot robot = Robot.getInstance();
		robot.rotateCounterClockwiseGivenDegrees(power, 90);
		robot.rotateClockwiseGivenDegrees(power, 180);
	}

	/*
	 * Drive in a straight line over 3 colored pieces of paper: red, then yellow,
	 * then blue stopping only when it is over the blue paper.
	 */
	private static void task3(int power) {
		System.out.println("Task 3");
		Robot robot = Robot.getInstance();
		robot.goForward(power);
		System.out.println("I'll stop once I see BLUE color.");
		while (!robot.overBlue()) {
		}
		robot.stop();
	}
}
