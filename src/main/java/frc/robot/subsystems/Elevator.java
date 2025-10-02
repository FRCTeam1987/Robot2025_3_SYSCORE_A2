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

import static edu.wpi.first.units.Units.*;
import static frc.robot.subsystems.constants.SubsystemConstants.ElevatorConstants.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DynamicMotionMagicVoltage;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import dev.doglog.DogLog;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.units.measure.*;
import frc.robot.RobotContainer;
import frc.robot.subsystems.constants.SubsystemConstants;
import frc.robot.utils.Conversions;

public class Elevator extends BroncSystem {

  public final TalonFX LEADER = new TalonFX(LEADER_CAN_ID, CAN_BUS);

  public final TalonFX FOLLOWER = new TalonFX(FOLLOWER_CAN_ID, CAN_BUS);

  private final StatusSignal<Angle> LEADER_POSITION = LEADER.getPosition();
  private final StatusSignal<Angle> FOLLOWER_POSITION = FOLLOWER.getPosition();
  private final StatusSignal<Current> LEADER_SUPPLY_CURRENT = LEADER.getSupplyCurrent();
  private final StatusSignal<Current> FOLLOWER_SUPPLY_CURRENT = FOLLOWER.getSupplyCurrent();
  private final StatusSignal<AngularVelocity> LEADER_VELOCITY = LEADER.getVelocity();

  private final Debouncer isAtTargetDebouncer = new Debouncer(0.06);
  private final Debouncer isNearTargetDebouncer = new Debouncer(0.06);

  private Distance target = Inches.of(0.0);
  private Distance distance = Inches.of(0.0);
  private boolean isAtTarget = false;
  private boolean isNearTarget = false;
  private boolean recovering = false;

  public Elevator() {

    TalonFXConfiguration ELEVATOR_CONFIG = elevatorConfig();
    LEADER.getConfigurator().apply(ELEVATOR_CONFIG);
    FOLLOWER.getConfigurator().apply(ELEVATOR_CONFIG);
    FOLLOWER.setControl(new Follower(LEADER.getDeviceID(), FOLLOWER_OPPOSE_LEADER));

    BaseStatusSignal.setUpdateFrequencyForAll(
        100.0,
        LEADER_POSITION,
        FOLLOWER_POSITION,
        LEADER_SUPPLY_CURRENT,
        FOLLOWER_SUPPLY_CURRENT,
        LEADER_VELOCITY);

    LEADER.setPosition(0.0);
    FOLLOWER.setPosition(0.0);
  }

  private void setConfig(final TalonFXConfiguration newConfig) {
    LEADER.getConfigurator().apply(newConfig);
    FOLLOWER.getConfigurator().apply(newConfig);
  }

  public void setConfigTeleop() {
    setConfig(SubsystemConstants.ElevatorConstants.elevatorConfig());
  }

  public void setConfigAuto() {
    setConfig(SubsystemConstants.ElevatorConstants.elevatorConfigFast());
  }

  @Override
  public void log() {

    if (RobotContainer.DEBUG) {
      DogLog.log("Elevator/leaderSupplyCurrent", LEADER_SUPPLY_CURRENT.getValueAsDouble());
      DogLog.log("Elevator/leaderPosition", LEADER_POSITION.getValueAsDouble());
      //      DogLog.log("Elevator/leaderVelocity", LEADER_VELOCITY.getValueAsDouble());
      DogLog.log("Elevator/followerSupplyCurrent", FOLLOWER_SUPPLY_CURRENT.getValueAsDouble());
      //      DogLog.log("Elevator/followerPosition", FOLLOWER_POSITION.getValueAsDouble());
      DogLog.log("Elevator/isAtTarget", isAtTarget);
      DogLog.log("Elevator/target", target.in(Meters));
      DogLog.log("Elevator/distance", distance.in(Meters));
         DogLog.log("Elevator/current", LEADER.getSupplyCurrent().getValueAsDouble());
         DogLog.log("Elevator/voltage", LEADER.getMotorVoltage().getValueAsDouble());
      // DogLog.log("Elevator/leaderIsConnected", leaderStatus.isOK());
      // DogLog.log("Elevator/followerIsConnected", followerStatus.isOK());
    }
  }

  @Override
  public void preCycle() {
    StatusCode allStatusCode =
        BaseStatusSignal.refreshAll(
            LEADER_POSITION,
            FOLLOWER_POSITION,
            LEADER_SUPPLY_CURRENT,
            FOLLOWER_SUPPLY_CURRENT,
            LEADER_VELOCITY);
    log();
    distance = Conversions.rotationsToMeters(LEADER_POSITION.getValue(), 1.0, PULLEY_RADIUS);
    isAtTarget = isAtTargetDebouncer.calculate(distance.isNear(target, Inches.of(0.5)));
    isNearTarget = isNearTargetDebouncer.calculate(distance.isNear(target, Inches.of(4.0)));
  }

  public void setDistance(Distance distance) {
    if (recovering) return;
    if (distance.gt(MAXIMUM_HEIGHT) || distance.lt(MINIMUM_HEIGHT)) return;
    if ((RobotContainer.DRIVETRAIN.getChassisSpeeds().vx > 2.0)
        || (RobotContainer.DRIVETRAIN.getChassisSpeeds().vy > 2.0)) return;
    Angle distanceAngle = Conversions.metersToRotations(distance, 1.0, PULLEY_RADIUS);
    if (LEADER.getPosition().getValue().lt(distanceAngle)) {

      LEADER.setControl(new DynamicMotionMagicVoltage(distanceAngle.in(Rotations), 235, 340, 0));
    } else {
      LEADER.setControl(new DynamicMotionMagicVoltage(distanceAngle.in(Rotations), 200, 45, 0));
    }
    target = distance;
  }

  public Distance getPosition() {
    return distance;
  }

  public boolean isAtTarget() {
    return isAtTarget;
  }

  public boolean isNearTarget() {
    return isNearTarget;
  }

  private Distance getTarget() {
    return target;
  }

  public void setRecovery(boolean recovery) {
    recovering = recovery;
  }

  public void zero() {
    LEADER.setPosition(0.0);
  }

  public void setVoltage(Voltage voltage) {
    LEADER.setControl(new VoltageOut(voltage));
  }

  public AngularVelocity getVelocity() {
    return LEADER_VELOCITY.getValue();
  }
}
