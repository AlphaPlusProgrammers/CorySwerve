/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import java.math.*;

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

    private TalonSRX turretingMotor; // The directional motor for the use of the 
                                     // drive motor.

    private Encoder turretingEncoder; // The encoder for the use of the 
                                      // turreting motor.
    private DigitalInput turretingProx; // The proximity sebsor for the use of 
                                        // the turreting motor.

    public static long directionTarget;

    private int usableEncoderCount;

    public static long encoderRemainingValue;

    /**
     * Sets the min and max output of the turreting motor
     * 
     * @param forward - (double) The forward peak output for the motor.
     * @param reverse - (double) The reverse peak output for the motor.
     * @return (void)
     */

    private void setMinMaxOutput(double forward, double reverse) {

        turretingMotor.configPeakOutputForward(forward);
        turretingMotor.configPeakOutputReverse(reverse);
    
    }

    /**
     * Takes the inputs and assigns the given source IDs to the 
     * corresponding device.
     * 
     * @param motorDeviceNumber - (int) CAN Device ID of the TalonSRX .
     * @param encSourceA - (int) The a channel digital input for the encoder.
     * @param encSourceB - (int) The b channel digital input for the encoder.
     * @param proxChannel - (int) The DIO channel digital input for the prox.
     * @return (void)
     */
    
    public AlphaMotor(int motorDeviceNumber, int encSourceA, 
                      int encSourceB, int proxChannel) {
        /**
         * Takes the inputs and assigns the given source IDs to the 
         * corresponding device.
         */
        
        turretingMotor = new TalonSRX(motorDeviceNumber);

        turretingEncoder = new Encoder(encSourceA, encSourceB);
        turretingProx = new DigitalInput(proxChannel);

        this.setMinMaxOutput(Constants.TURRET_SPEED_MAX_OUTPUT, 
                             Constants.TURRET_SPEED_MIN_OUTPUT);

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
     * @return (int) The calulated quadrant corresponding to the user input.
     * (IllegalArgumentException) The error thrown if the x or y given equaled
     * 0. If either variable lies on an axis, the output is not in a quadrant.
     */

    private int quadrant (double x, double y){
        if (x > 0 && y > 0){
            return 1;
        } else if (x < 0 && y > 0){
            return 2;
        } else if (x < 0 && y < 0){
            return 3;
        } else if (x > 0 && y < 0){
            return 4;
        } else {
            throw new IllegalArgumentException(
                      "Neither X or Y may lie on an axis.");
        }
    }

    /**
     * Calulates the positon desired in terms of encoder ticks.
     * 
     * @param x - (double) The value of the x axis of the joystick.
     * @param y - (double) The value of the y axis of the joystick.
     * @return (long) The value in ticks that corresponds to the inputs.
     */

    private long desiredTargetTicks(double x, double y) { 

        /**
         * Math Explanation
         * 
         * Math = (((Math.PI/2) - (Math.atan(y/x)))/(Math.PI/2))
         * 
         * The process finds theta "(Math.atan(y/x))", the roation in radians.
         * Then rotates it up by 90 degrees "(Math.PI/2) -", in order to 
         * put "0" as north.
         * It then converts that to a ratio "/(Math.PI/2)", 
         * the calulated position / the total position, to later be multiplied 
         * by the number of ticks in 90 degrees of movement 
         * "Constants.ENCODER_TICKS_IN_QUADRANT".
         */


        double multiplier = 0.0; // Initialize multiplier

        if (x >= -Constants.STICK_ERROR &&
            x <= Constants.STICK_ERROR &&
            y >= -Constants.STICK_ERROR &&
            y <= Constants.STICK_ERROR) {
            
            multiplier = 0; // if stick is due east, then desired = number of 
                            // ticks in quadrant
            
        } else if (x >= 0 && 
                   y >= -Constants.STICK_ERROR &&
                   y <= Constants.STICK_ERROR) {
        
        multiplier = 1; // if stick is due east, then desired = number of 
                        // ticks in quadrant
        
        } else if (x <= 0 && 
                  y >= -Constants.STICK_ERROR &&
                  y <= Constants.STICK_ERROR) {
        
            multiplier = 3; // if stick is due west, then desired = number of 
                            // ticks in quadrant * 3
        
        } else if (y >= 0 && 
                   x >= -Constants.STICK_ERROR &&
                   x <= Constants.STICK_ERROR) {

            multiplier = 0; // if stick is due north, then desired = 0

        } else if (y <= 0 && 
                  x >= -Constants.STICK_ERROR &&
                  x <= Constants.STICK_ERROR) {

            multiplier = 2; // if stick is due south, then desired = number of 
                            // ticks in quadrant * 2

        } else if (quadrant(x, y) == 1 || quadrant(x, y) == 4){
               // if stick is in quadrants 1 or 4 it uses the direct ratio

            multiplier = (((Math.PI/2) - (Math.atan(y/x)))/(Math.PI/2));

        } else if (quadrant(x, y) == 2 || quadrant(x, y) == 3){
               // if stick is in quadrants 1 or 4 it uses the ratio + 2, to 
               // rotate by 180 degrees

            multiplier = (2 + ((Math.PI/2) - (Math.atan(y/x)))/(Math.PI/2));

        } 
        
        directionTarget = Math.round(Constants.ENCODER_TICKS_IN_QUADRANT * multiplier);
        return directionTarget;
    
    }

    private int currentEncoderCount(){

        return turretingEncoder.get();

    }

    private void moveMotor(double speed) {
        turretingMotor.set(ControlMode.PercentOutput, speed);
    }

    private void stopMotors() {
        turretingMotor.set(ControlMode.PercentOutput, 0);
    }

    public void pointToTarget(double targetX, double targetY){

        int usableEncoderCount = currentEncoderCount();

        long desiredTarget = desiredTargetTicks(targetX, targetY);
        long desiredRemaing = usableEncoderCount - desiredTarget;
        

        if (desiredRemaing > Constants.ENCODER_TICKS_IN_QUADRANT * 2) {

            usableEncoderCount = ((Constants.ENCODER_TICKS_IN_QUADRANT * 4) - 
                                  usableEncoderCount);
            desiredRemaing = usableEncoderCount - desiredTarget;
            
        }
        if(desiredRemaing != 0){
            long directionalMultiplier = (desiredRemaing / Math.abs(desiredRemaing));


            if (Math.abs(desiredRemaing) > Constants.LARGE_SWERVE_ROTATION_ERROR) {
                moveMotor(Constants.FAST_SWERVE_ROTATION_SPEED * directionalMultiplier);
            } else if (Math.abs(desiredRemaing) > Constants.SMALL_SWERVE_ROTATION_ERROR) {
                moveMotor(Constants.SLOW_SWERVE_ROTATION_SPEED * directionalMultiplier);
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
        turretingEncoder.reset();
      }
    
    
    
      public boolean proxValue() {
        return !turretingProx.get();
      }

      
    public int encoderValue() {
        return turretingEncoder.get();
    }

      public void findZero() {
        int i = 0;
        while(!proxValue()) {
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
          moveMotor(Constants.SLOW_SWERVE_ROTATION_SPEED * (encoderRemaining(desiredTarget, false)/encoderRemaining(desiredTarget, true)));
        } else {
          moveMotor(Constants.FAST_SWERVE_ROTATION_SPEED * (encoderRemaining(desiredTarget, false)/encoderRemaining(desiredTarget, true)));
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
