/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean constants. This class should not be used for any other
 * purpose. All constants should be declared globally (i.e. public static). Do
 * not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the constants are needed, to reduce verbosity.
 */
public final class Constants {

    public static int TURRET_SPEED_MAX_OUTPUT = 1;
    public static int TURRET_SPEED_MIN_OUTPUT = -1;
    public static double STICK_ERROR = 0.05;
    public static int ENCODER_TICKS_IN_QUADRANT = 105; // Can be modified if the number of ticks seems to change, 105 per quad = 420 per revolution
    public static int LARGE_SWERVE_ROTATION_ERROR = 10; // These are modified if you expierence jitter, this value reduces how far the tire will spin at a fast speed
    public static int SMALL_SWERVE_ROTATION_ERROR = 5; // This value is used as a hard stop, if the wheel gets within +- this value it will stop the rotation
    public static double FAST_SWERVE_ROTATION_SPEED = 0.5;
    public static double SLOW_SWERVE_ROTATION_SPEED = 0.1;

    public static int driverButtonA = 1; // Target With Limelight Button - Held
    public static int driverButtonB = 2;
    public static int driverButtonX = 3;
    public static int driverButtonY = 4; // Targeting Button - Held
    public static int driverButtonLB = 5; // Strafe Easy Mode Button - Held
    public static int driverButtonRB = 6; // Point Turn Button - Held
    public static int driverButtonBack = 7;
    public static int driverButtonStart = 8;
    public static int driverButtonLeftJoyClick = 9;
    public static int driverButtonRightJoyClick = 10;

    public static int driverLeftAxisX = 0;
    public static int driverLeftAxisY = 1;
    public static int driverRightAxisX = 4;
    public static int driverRightAxisY = 5;
    public static int driverLeftAxisTrigger = 2;
    public static int driverRightAxisTrigger = 3;

}
