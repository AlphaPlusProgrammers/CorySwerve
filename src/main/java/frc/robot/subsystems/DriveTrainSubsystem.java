/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class DriveTrainSubsystem extends SubsystemBase {
    /**
     * Creates a new DriveTrainSubsystem.
     */

    private TalonFX frontLeftDriveMotor;
    private TalonFX frontRightDriveMotor;
    private TalonFX rearLeftDriveMotor;
    private TalonFX rearRightDriveMotor;

    public AlphaMotor motorFL;
    public AlphaMotor motorFR;
    public AlphaMotor motorRL;
    public AlphaMotor motorRR;

    public DriveTrainSubsystem() {
        motorFL = new AlphaMotor(2, 12, 10, 0);
        motorFR = new AlphaMotor(4, 18, 17, 1);
        motorRL = new AlphaMotor(6, 14, 13, 2);
        motorRR = new AlphaMotor(8, 16, 15, 3);

        frontLeftDriveMotor = new TalonFX(1);
        frontRightDriveMotor = new TalonFX(3);
        rearLeftDriveMotor = new TalonFX(5);
        rearRightDriveMotor = new TalonFX(7);
    }

    @Override
    public void periodic() {
        // This method will be called once per scheduler run
    }

    public void moveSwerveAxis(double leftX, double leftY, double rightX, double rightY, double leftT, double rightT) {

        leftSwerves(leftX, leftY);

        if ((Math.abs(leftX - rightX) < 0.15 && Math.abs(leftY - rightY) < 0.15) && leftX > 0.15 && leftY > 0.15
                && rightX > 0.15 && rightY > 0.15) {
            rightSwerves(leftX, leftY);
        } else {
            rightSwerves(rightX, rightY);
        }

        double frontDesiredSpeed = findSpeed(rightT, leftT);
        double rearDesiredSpeed = frontDesiredSpeed;

        if (leftY < 0) {
            frontDesiredSpeed = -frontDesiredSpeed;
        }

        if (rightY < 0) {
            rearDesiredSpeed = -rearDesiredSpeed;
        }

        moveDriveMotors(frontDesiredSpeed, rearDesiredSpeed);

    }

    private void leftSwerves(double x, double y) {
        motorFL.pointToTarget(x, y);
        motorFR.pointToTarget(x, y);
    }

    private void rightSwerves(double x, double y) {
        motorRL.pointToTarget(x, y);
        motorRR.pointToTarget(x, y);
    }

    private double findSpeed(double positive, double negitive) {
        return (positive - negitive) / 2;
    }

    private void moveDriveMotors(double frontSpeed, double rearSpeed) {
        frontLeftDriveMotor.set(ControlMode.PercentOutput, frontSpeed);
        frontRightDriveMotor.set(ControlMode.PercentOutput, frontSpeed);
        rearLeftDriveMotor.set(ControlMode.PercentOutput, rearSpeed);
        rearRightDriveMotor.set(ControlMode.PercentOutput, rearSpeed);

        frontLeftDriveMotor.setInverted(true);
        frontRightDriveMotor.setInverted(false);
        rearLeftDriveMotor.setInverted(true);
        rearRightDriveMotor.setInverted(false);
    }

    public void zeroAllEncoders() {
        motorFL.zeroEncoder();
        motorFR.zeroEncoder();
        motorRL.zeroEncoder();
        motorRR.zeroEncoder();
    }

    public void findAllZeros() {
        motorFL.findZero();
        motorFR.findZero();
        motorRL.findZero();
        motorRR.findZero();
    }

    public void zeroAllEncodersBasedOnProx() {
        motorFL.zeroEncoderBasedOnProx();
        motorFR.zeroEncoderBasedOnProx();
        motorRL.zeroEncoderBasedOnProx();
        motorRR.zeroEncoderBasedOnProx();
    }
}
