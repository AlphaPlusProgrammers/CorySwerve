/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class AlphaMotor extends SubsystemBase {
    /**
     * Creates a new AlphaMotor.
     */

    private TalonSRX rotationMotor; // The directional motor for the use of the
                                    // drive motor.

    private Encoder rotationEncoder; // The encoder for the use of the
                                     // turreting motor.
    private DigitalInput rotationProx; // The proximity sebsor for the use of
                                       // the turreting motor.

    public long directionTarget;

    public long encoderRemainingValue;

    /**
     * Sets the min and max output of the turreting motor
     * 
     * @param forward - (double) The forward peak output for the motor.
     * @param reverse - (double) The reverse peak output for the motor.
     * @return (void)
     */

    private void setMinMaxOutput(double forward, double reverse) {

        rotationMotor.configPeakOutputForward(forward);
        rotationMotor.configPeakOutputReverse(reverse);

    }

    /**
     * Takes the inputs and assigns the given source IDs to the corresponding
     * device.
     * 
     * @param motorDeviceNumber - (int) CAN Device ID of the TalonSRX .
     * @param encSourceA        - (int) The a channel digital input for the encoder.
     * @param encSourceB        - (int) The b channel digital input for the encoder.
     * @param proxChannel       - (int) The DIO channel digital input for the prox.
     * @return (void)
     */

    public AlphaMotor(int motorDeviceNumber, int encSourceA, int encSourceB, int proxChannel) {
        /**
         * Takes the inputs and assigns the given source IDs to the corresponding
         * device.
         */

        rotationMotor = new TalonSRX(motorDeviceNumber); //

        rotationEncoder = new Encoder(encSourceA, encSourceB);
        rotationProx = new DigitalInput(proxChannel);

        this.setMinMaxOutput(Constants.TURRET_SPEED_MAX_OUTPUT, Constants.TURRET_SPEED_MIN_OUTPUT);

    }

    @Override
    public void periodic() {
        // This method will be called once per scheduler run
    }

    /**
     * Calulates the quadrant of the users input.
     * 
     * @param x - (double) The value of the x axis of the joystick.
     * @param y - (double) The value of the y axis of the joystick.
     * 
     * @return (int) The calulated quadrant corresponding to the user input.
     *         (IllegalArgumentException) The error thrown if the x or y given
     *         equaled 0. If either variable lies on an axis, the output is not in a
     *         quadrant.
     */

    private int quadrant(double x, double y) {
        if (x > 0 && y > 0) {
            return 1;
        } else if (x < 0 && y > 0) {
            return 2;
        } else if (x < 0 && y < 0) {
            return 3;
        } else if (x > 0 && y < 0) {
            return 4;
        } else {
            throw new IllegalArgumentException("Neither X or Y may lie on an axis.");
        }
    }

    /**
     * Calulates the position desired in terms of encoder ticks.
     * 
     * @param x - (double) The value of the x axis of the joystick.
     * @param y - (double) The value of the y axis of the joystick.
     * @return (long) The value in ticks that corresponds to the inputs.
     */

    private long desiredTargetTicks(double x, double y) {

        /*
         * Math Explanation
         * 
         * Math = (((Math.PI/2) - (Math.atan(y/x)))/(Math.PI/2))
         * 
         * The process finds theta "(Math.atan(y/x))", the roation in radians. Then
         * rotates it up by 90 degrees "(Math.PI/2) -", in order to put "0" as north. It
         * then converts that to a ratio "/(Math.PI/2)", the calulated position / the
         * total position, to later be multiplied by the number of ticks in 90 degrees
         * of movement "Constants.ENCODER_TICKS_IN_QUADRANT".
         */

        double multiplier = 0.0; // Initialize multiplier

        if (x >= -Constants.STICK_ERROR && x <= Constants.STICK_ERROR && y >= -Constants.STICK_ERROR
                && y <= Constants.STICK_ERROR) {

            multiplier = 0; // if stick is due east, then desired = number of
                            // ticks in quadrant

        } else if (x >= 0 && y >= -Constants.STICK_ERROR && y <= Constants.STICK_ERROR) {

            multiplier = 1; // if stick is due east, then desired = number of
                            // ticks in quadrant

        } else if (x <= 0 && y >= -Constants.STICK_ERROR && y <= Constants.STICK_ERROR) {

            multiplier = 3; // if stick is due west, then desired = number of
                            // ticks in quadrant * 3

        } else if (y >= 0 && x >= -Constants.STICK_ERROR && x <= Constants.STICK_ERROR) {

            multiplier = 0; // if stick is due north, then desired = 0

        } else if (y <= 0 && x >= -Constants.STICK_ERROR && x <= Constants.STICK_ERROR) {

            multiplier = 2; // if stick is due south, then desired = number of
                            // ticks in quadrant * 2

        } else {
            int curr_quad = quadrant(x, y);
            if (curr_quad == 1 || curr_quad == 4) {
                // if stick is in quadrants 1 or 4 it uses the direct ratio

                multiplier = (((Math.PI / 2) - (Math.atan(y / x))) / (Math.PI / 2));

            } else if (curr_quad == 2 || curr_quad == 3) {
                // if stick is in quadrants 1 or 4 it uses the ratio + 2, to
                // rotate by 180 degrees

                multiplier = (2 + ((Math.PI / 2) - (Math.atan(y / x))) / (Math.PI / 2));

            }
        }

        directionTarget = Math.round(Constants.ENCODER_TICKS_IN_QUADRANT * multiplier);
        return directionTarget;

    }

    /**
     * Grabs the current encoder count (in ticks) and reseting after crossing 1
     * rotations worth of ticks
     * 
     * @return (int) The rotation motor's encoder count
     */

    private int currentEncoderCount() {

        if (rotationEncoder.get() > 0) {
            return rotationEncoder.get();
        } else  {
            return rotationEncoder.get() + 420;
        }

    }

    /**
     * Applies a desired power level to the rotation motor
     * 
     * @param speed - (double) the desired power in percentage [-1, 1]
     * 
     * @return (void)
     */

    private void moveMotor(double speed) {
        rotationMotor.set(ControlMode.PercentOutput, speed);
    }

    /**
     * Applies a power of 0 to the rotation motor
     * 
     * @return (void)
     */

    private void stopMotors() {
        rotationMotor.set(ControlMode.PercentOutput, 0);
    }

    /**
     * Takes a joystick input and turns the rotation motor to the equivilant of that
     * position in ticks
     * 
     * @param targetX - (double) The joystick's current x-value
     * @param targetY - (double) The joystick's current y-value
     * 
     * @return (void)
     */

    public void pointToTarget(double targetX, double targetY) {

        int currentPosition = currentEncoderCount();
        long desiredTarget = desiredTargetTicks(targetX, targetY); // The target position in ticks
        long targetDifference = desiredTarget - currentPosition; // The distance between the current position and the desired target
        int directionalMultiplier = 0; // A placeholder to determine the direction of the motor

        if ((targetDifference != 0) || (targetDifference - 420 != 0) || (targetDifference + 420 != 0)) {// As long as we actually have a different target to go to, we continue to
                                    // actually go to the desired target

            if (targetDifference > 210) {
                //directionalMultiplier = Math.round((targetDifference - 420) / Math.abs(targetDifference - 420));
                if(targetDifference-420 < 0){
                    directionalMultiplier = -1;
                }else{
                    directionalMultiplier = 1;
                }
            } else if (targetDifference < -210){
                //directionalMultiplier = Math.round((targetDifference + 420) / Math.abs(targetDifference + 420));
                if(targetDifference+420 < 0){
                    directionalMultiplier = -1;
                }else{
                    directionalMultiplier = 1;
                }
            } else {
               // directionalMultiplier = Math.round((targetDifference) / Math.abs(targetDifference));
               if(targetDifference < 0){
                   directionalMultiplier = -1;
               }else{
                   directionalMultiplier = 1;
               }
            }

            if (Math.abs(targetDifference) > Constants.LARGE_SWERVE_ROTATION_ERROR) {
                moveMotor(Constants.FAST_SWERVE_ROTATION_SPEED * -directionalMultiplier);
            } else if (Math.abs(targetDifference) > Constants.SMALL_SWERVE_ROTATION_ERROR) {
                moveMotor(Constants.SLOW_SWERVE_ROTATION_SPEED * -directionalMultiplier);
            } else {
                stopMotors();
            }
            
        }
    }

    public void zeroEncoderBasedOnProx() {
        if (proxValue()) {
            zeroEncoder();
        }
    }

    public void zeroEncoder() {
        rotationEncoder.reset();
    }

    public boolean proxValue() {
        return !rotationProx.get();
    }

    public int encoderValue() {
        return rotationEncoder.get();
    }

    public void findZero() {
        int i = 0;
        while (!proxValue()) {
            double speed = Constants.FAST_SWERVE_ROTATION_SPEED;
            if (encoderValue() < 0 && i == 0) {
                speed = -speed;
                i++;
            }
            moveMotor(speed);
        }
        zeroEncoder();
        swerveDatBoi(0);
        i = 0;
    }

    public void swerveDatBoi(long desiredTarget) {
        if (encoderRemaining(desiredTarget, true) < Constants.SLOW_SWERVE_ROTATION_SPEED) {
            stopMotors();
        } else if (encoderRemaining(desiredTarget, true) < Constants.FAST_SWERVE_ROTATION_SPEED) {
            moveMotor(Constants.SLOW_SWERVE_ROTATION_SPEED
                    * (encoderRemaining(desiredTarget, false) / encoderRemaining(desiredTarget, true)));
        } else {
            moveMotor(Constants.FAST_SWERVE_ROTATION_SPEED
                    * (encoderRemaining(desiredTarget, false) / encoderRemaining(desiredTarget, true)));
        }
    }

    private long encoderRemaining(long targetValue, boolean abs) {

        if (abs) {
            encoderRemainingValue = Math.abs(targetValue - encoderValue());
        } else {
            encoderRemainingValue = targetValue - encoderValue();
        }

        return encoderRemainingValue;
    }

}
