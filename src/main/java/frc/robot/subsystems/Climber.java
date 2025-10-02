// Copyright FRC 5712
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static frc.robot.subsystems.constants.SubsystemConstants.ClimberConstants.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import dev.doglog.DogLog;
import edu.wpi.first.units.AngularVelocityUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.GenericHID;
import frc.robot.RobotContainer;
import frc.robot.state.Abomination;
import frc.robot.state.commands.AsyncRumble;
import frc.robot.state.logic.mode.ScoreMode;
import frc.robot.subsystems.constants.SubsystemConstants.ClimberConstants;

import java.util.function.Supplier;

public class Climber extends BroncSystem {

  public final TalonFX LEADER = new TalonFX(LEADER_MOTOR_ID, CANBUS_NAME);

  public final TalonFX ROLLER = new TalonFX(ROLLER_MOTOR_ID);

  public final CANcoder ENCODER = new CANcoder(ENCODER_ID, CANBUS_NAME);

  // public final LaserCan LASER_L = new LaserCan(LASER_L_ID);
  // ;
  // public final LaserCan LASER_R = new LaserCan(LASER_R_ID);
  // ;

  // public final Supplier<LaserCanInterface.Measurement> LASER_L_DATA = () -> getMeasurement(LASER_L);
  // public final Supplier<LaserCanInterface.Measurement> LASER_R_DATA = () -> getMeasurement(LASER_R);

  private final StatusSignal<Angle> ENCODER_POSITION = ENCODER.getPosition();
  private final StatusSignal<Angle> LEADER_POSITION = LEADER.getPosition();
  private final StatusSignal<Current> LEADER_SUPPLY_CURRENT = LEADER.getSupplyCurrent();

  private Angle target = FULLY_STOWED;
  private Angle current;

  private boolean isCoast;
  private boolean isStopped;

  public Climber() {
    TalonFXConfiguration config = climberConfig();
    ENCODER.getConfigurator().apply(encoderConfig());
    LEADER.getConfigurator().apply(config);
    ROLLER.getConfigurator().apply(rollerConfig());
    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0, LEADER_POSITION, LEADER_SUPPLY_CURRENT, ENCODER_POSITION);
    LEADER.setPosition(0.0);
    isCoast = config.MotorOutput.NeutralMode.equals(NeutralModeValue.Coast);
    isStopped = true;
  }

  // public LaserCanInterface.Measurement getMeasurement(LaserCan device) {
  //   if (device.getMeasurement() != null && device.getMeasurement().ambient < 100) {
  //     return device.getMeasurement();
  //   }
  //   return new LaserCanInterface.Measurement(
  //       4, 200, 0, false, 0, new LaserCanInterface.RegionOfInterest(0, 0, 0, 0));
  // }

  @Override
  public void preCycle() {
    log();
    if (ENCODER_POSITION.getValue().lt(Degrees.of(89))) {
      LEADER.stopMotor();
    }
    // if (Abomination.getScoreMode() == ScoreMode.CLIMB) {
    //   if (LASER_R_DATA.get().distance_mm <= 70)
    //     new AsyncRumble(
    //             RobotContainer.JOYSTICK.getHID(), GenericHID.RumbleType.kRightRumble, 1.0, 150)
    //         .schedule();

    //   if (LASER_L_DATA.get().distance_mm <= 70)
    //     new AsyncRumble(
    //             RobotContainer.JOYSTICK.getHID(), GenericHID.RumbleType.kLeftRumble, 1.0, 150)
    //         .schedule();
    // }
    //        double voltage = Math.abs(getPosition().minus(target).in(Rotations)) * 100;
    //        if (getPosition().lt(FULLY_STOWED)) {
    //
    //            System.out.println("nope");
    //            return;
    //        }
    //        if (getPosition().gt(FULLY_EXTENDED)) {
    //            System.out.println("nope");
    //            return;
    //        }
    //        if (getPosition().gt(target)) {
    //            LEADER.setControl(new VoltageOut(voltage));
    //            System.out.println("GOING TO" + voltage);
    //        } else {
    //            LEADER.setControl(new VoltageOut(-voltage));
    //            System.out.println("GOING TO -" + voltage);
    //        }
  }

  public static final PositionVoltage mmDeploy = new PositionVoltage(FULLY_EXTENDED);

  public static final VoltageOut runRollers = new VoltageOut(ROLLER_VOLTAGE);
  public static final VoltageOut stopRollers = new VoltageOut(0);

  public void deploy() {
    setPosition(mmDeploy);
    ROLLER.setControl(runRollers);
    // setPosition(FULLY_EXTENDED);
  }

  public static final PositionVoltage mmClimb = new PositionVoltage(FULLY_CLIMBED);

  public void climb() {
    setPosition(mmClimb);
    new AsyncRumble(RobotContainer.JOYSTICK.getHID(), GenericHID.RumbleType.kBothRumble, 1.0, 1000L).schedule();
    ROLLER.setControl(stopRollers);
    // setPosition(FULLY_CLIMBED);

    
  }

  public static final PositionVoltage mmStow = new PositionVoltage(FULLY_STOWED);

  public void stow() {
    setPosition(mmStow);
    ROLLER.setControl(stopRollers);
    // setPosition(FULLY_STOWED);
  }

  public void stop() {
    if (isStopped) {
      return;
    }
    LEADER.stopMotor();
    isStopped = true;
  }

  public void coast() {
    if (isCoast) {
      return;
    }
    LEADER
        .getConfigurator()
        .apply(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast));
    isCoast = true;
  }

  public void brake() {
    if (!isCoast) {
      return;
    }
    LEADER
        .getConfigurator()
        .apply(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake));
    isCoast = false;
  }

  public boolean isAtTarget() {
    return getPosition().isNear(target, Degrees.of(5.0)); // was 0.5, new climber doesn't seem to be as consistent
  }

  public AngularVelocity getRollerRPS() {
    return ROLLER.getVelocity().getValue();
  }

  public boolean hasCage() {
    return getRollerRPS().lt(ClimberConstants.ROLLER_VELOCITY_THRESHOLD);
  }

  @Override
  public void log() {
    StatusCode allStatus =
        BaseStatusSignal.refreshAll(LEADER_POSITION, LEADER_SUPPLY_CURRENT, ENCODER_POSITION);
    // StatusCode leaderStatus = BaseStatusSignal.refreshAll(LEADER_POSITION,
    // LEADER_SUPPLY_CURRENT);

    // StatusCode encoderStatus = BaseStatusSignal.refreshAll(ENCODER_POSITION);

    if (RobotContainer.DEBUG) {
      DogLog.log("Climber/leaderPosition", LEADER_POSITION.getValueAsDouble());
      DogLog.log("Climber/encoderPosition", ENCODER_POSITION.getValueAsDouble());
      DogLog.log("Climber/encoderDegrees", Degrees.convertFrom(ENCODER_POSITION.getValueAsDouble(), Rotations));
      DogLog.log("Climber/leaderCurrent", LEADER_SUPPLY_CURRENT.getValueAsDouble());
      // if (Abomination.getScoreMode() == ScoreMode.CLIMB) {
      //   DogLog.log("Climber/laserLDist", LASER_L_DATA.get().distance_mm);
      //   DogLog.log("Climber/laserRDist", LASER_R_DATA.get().distance_mm);
      //   DogLog.log("Climber/laserRTrip", LASER_R_DATA.get().distance_mm <= 70);
      //   DogLog.log("Climber/laserLTrip", LASER_L_DATA.get().distance_mm <= 70);
      // }
      DogLog.log("Climber/isAtTarget", isAtTarget());
      DogLog.log("Climber/target", target.in(Rotations));
      DogLog.log("Climber/rollerRPS", getRollerRPS().baseUnitMagnitude());
    }
  }

  public void setPosition(PositionVoltage positionControl) {
    if (isStopped) {
      isStopped = false;
    }
    LEADER.setControl(positionControl);
    target = positionControl.getPositionMeasure();
  }

  public void setPosition(Angle pos) {
    if (isStopped) {
      isStopped = false;
      return;
    }
    LEADER.setControl(new MotionMagicVoltage(pos));
    target = pos;
  }

  public Angle getPosition() {
    return ENCODER_POSITION.getValue();
  }

  public void setVoltage(Voltage volts) {
    LEADER.setControl(new VoltageOut(volts));
  }
}
