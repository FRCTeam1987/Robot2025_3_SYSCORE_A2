package frc.robot.subsystems.constants;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.signals.ReverseLimitSourceValue;
import com.ctre.phoenix6.signals.ReverseLimitTypeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.*;
import edu.wpi.first.wpilibj.util.Color8Bit;
import frc.robot.state.Abomination;
import frc.robot.state.logic.constants.MechanismConstant;
import java.util.List;

public class SubsystemConstants {
  public static class ElevatorConstants {

    // CANBus constants
    public static final CANBus CAN_BUS = new CANBus("canfd");
    public static final int LEADER_CAN_ID = 8;
    public static final int FOLLOWER_CAN_ID = 19;

    // Ratios & calculations
    public static final double ELEVATOR_REDUCTION = (54.0 / 8.0);
    public static final double PULLEY_DIAMETER_INCHES = 1.88;
    public static final Distance PULLEY_RADIUS = Inches.of(PULLEY_DIAMETER_INCHES / 2.0);

    // Mechanism properties
    public static final boolean FOLLOWER_OPPOSE_LEADER = false;
    public static final Distance MINIMUM_HEIGHT = Inches.of(0.0);
    public static final Distance MAXIMUM_HEIGHT = Inches.of(50.5);

    // Simulation properties
    public static final Mass MOVING_MASS = Pounds.of(10.0);

    // Configs
    public static TalonFXConfiguration elevatorConfig() {
      final TalonFXConfiguration CFG = new TalonFXConfiguration();

      // MotorOutput
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Brake);

      // Slot0
      CFG.Slot0.withKP(15.0);
      CFG.Slot0.withKI(0.0);
      CFG.Slot0.withKD(0.1);
      CFG.Slot0.withKS(0.0);
      CFG.Slot0.withKV(0.15);
      CFG.Slot0.withKA(0.0);
      CFG.Slot0.withKG(0.15);
      CFG.Slot0.withGravityType(GravityTypeValue.Elevator_Static);

      // Feedback
      CFG.Feedback.withSensorToMechanismRatio(ELEVATOR_REDUCTION);

      // CurrentLimits
      CFG.CurrentLimits.withSupplyCurrentLimit(Amps.of(32)); // was 32
      CFG.CurrentLimits.withSupplyCurrentLimitEnable(true);

      return CFG;
    }

    public static TalonFXConfiguration elevatorConfigFast() {
      TalonFXConfiguration fastConfig = elevatorConfig();
      fastConfig.CurrentLimits.withSupplyCurrentLimit(Amps.of(40.0));
      return fastConfig;
    }
  }

  public static class ArmConstants {
    // CANBus constants
    public static final int CORAL_CANDI_ID = 1;
    public static final int ALGAE_CANDI_ID = 2;
    public static final int ENCODER_ID = 5;
    public static final int LEADER_MOTOR_ID = 12;
    public static final int FOLLOWER_MOTOR_ID = 11;
    public static final String CANBUS_NAME = "canfd";

    // Ratios and calculations
    public static final double GEAR_RATIO = 30;
    public static final double ARM_RATIO = (60.0 / 12.0) * (60.0 / 18.0) * (96.0 / 20.0);
    public static final double EFFECTOR_RATIO = (48.0 / 15.0);
    public static final Distance SLOW_HEIGHT =
        MechanismConstant.L4
            .getElevatorDistance()
            .minus(MechanismConstant.L3.getElevatorDistance());

    // Sensor configurations
    public static final double CORAL_PIECE_DEBOUNCE_SECONDS = 0.1;
    public static final DebounceType CORAL_DEBOUNCE_TYPE = DebounceType.kBoth;
    public static final double ALGAE_PIECE_DEBOUNCE_SECONDS = 0.45;
    public static final DebounceType ALGAE_DEBOUNCE_TYPE = DebounceType.kBoth;

    // Mechanism properties
    public static final Angle ARM_MAGNET_OFFSET = Rotations.of(-0.506348);

    // Dynamic configs
    public static final AngularAcceleration FAST_ACCEL = RotationsPerSecondPerSecond.of(25);
    public static final AngularVelocity FAST_CRUISE = RotationsPerSecond.of(9.0);
    public static final AngularAcceleration SLOW_ACCEL = RotationsPerSecondPerSecond.of(8.0);
    public static final AngularVelocity SLOW_CRUISE = RotationsPerSecond.of(4.5);
    public static final Velocity<AngularAccelerationUnit> CARNAGE =
        RotationsPerSecondPerSecond.per(Second).of(0.0);

    // Configs
    public static CANcoderConfiguration encoderConfig() {
      final CANcoderConfiguration CFG = new CANcoderConfiguration();
      // MagnetSensor
      CFG.MagnetSensor.withSensorDirection(SensorDirectionValue.CounterClockwise_Positive);
      CFG.MagnetSensor.withMagnetOffset(ARM_MAGNET_OFFSET);
      CFG.MagnetSensor.withAbsoluteSensorDiscontinuityPoint(Rotations.of(1.0));

      return CFG;
    }

    public static TalonFXConfiguration armConfig() {
      final TalonFXConfiguration CFG = new TalonFXConfiguration();

      // MotorOutput
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Brake); // TODO: brake for competition

      // Slot0
      CFG.Slot0.withKP(103);
      CFG.Slot0.withKI(0.0);
      CFG.Slot0.withKD(0.1);

      // ClosedLoop
      CFG.ClosedLoopGeneral.withContinuousWrap(false);

      // SoftwareLimitSwitch
      CFG.SoftwareLimitSwitch.withForwardSoftLimitThreshold(Rotations.of(1.0));
      CFG.SoftwareLimitSwitch.withForwardSoftLimitEnable(true);
      CFG.SoftwareLimitSwitch.withReverseSoftLimitThreshold(Rotations.of(-1.0));
      CFG.SoftwareLimitSwitch.withReverseSoftLimitEnable(true);

      // Feedback
      CFG.Feedback.withFeedbackRemoteSensorID(ENCODER_ID);
      CFG.Feedback.withSensorToMechanismRatio(1.0);
      CFG.Feedback.withRotorToSensorRatio(ARM_RATIO);
      CFG.Feedback.withFeedbackSensorSource(FeedbackSensorSourceValue.FusedCANcoder);

      // CurrentLimits
      CFG.CurrentLimits.withStatorCurrentLimit(Amps.of(30.0));
      CFG.CurrentLimits.withStatorCurrentLimitEnable(true);

      return CFG;
    }

    public static TalonFXConfiguration effectorConfig() {
      TalonFXConfiguration CFG = new TalonFXConfiguration();

      // MotorOutput
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Brake);

      // Slot0
      CFG.Slot0.withKP(40);
      CFG.Slot0.withKI(0.0);
      CFG.Slot0.withKD(0.1);
      CFG.Slot0.withKV(0.08);
      CFG.Slot0.withKS(2.0);

      // Slot1
      CFG.Slot1.withKP(0.003);
      CFG.Slot1.withKI(0.0);
      CFG.Slot1.withKD(0.0);

      // CurrentLimits
      CFG.CurrentLimits.withSupplyCurrentLimit(Amps.of(20.0));
      CFG.CurrentLimits.withSupplyCurrentLowerTime(Seconds.of(4.0));
      CFG.CurrentLimits.withSupplyCurrentLimitEnable(false);

      // Feedback
      CFG.Feedback.withSensorToMechanismRatio(EFFECTOR_RATIO);

      return CFG;
    }
  }

  public static class IntakeConstants {
    public static final int MOTOR_ID = 20;
    public static final String CAN_BUS = "can_s0";
    public static final double MOTOR_CONNECTED_DEBOUNCE_SECONDS = 0.5;
    public static final double MOTOR_RUN_VOLTS = -10;

    public static TalonFXConfiguration intakeConfig() {
      TalonFXConfiguration CFG = new TalonFXConfiguration();
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Coast);
      CFG.CurrentLimits.withSupplyCurrentLimit(Amps.of(20.0));
      CFG.CurrentLimits.withSupplyCurrentLowerTime(Seconds.of(2.0));
      CFG.CurrentLimits.withSupplyCurrentLimitEnable(true);
      return CFG;
    }
  }

  public static class ClimberConstants {
    public static final int LEADER_MOTOR_ID = 9;
    public static final int ROLLER_MOTOR_ID = 10;
    public static final String CANBUS_NAME = "canfd";
    public static final int ENCODER_ID = 6;
    public static final int LASER_L_ID = 2;
    public static final int LASER_R_ID = 1;

    public static final Angle FULLY_STOWED = Degrees.of(90.0);
    public static final Angle FULLY_EXTENDED = Degrees.of(174.5); // was 180.0
    public static final Angle FULLY_CLIMBED = Degrees.of(94.5); // was 93.0

    public static final Voltage ROLLER_VOLTAGE = Volts.of(10.0);
    public static final AngularVelocity ROLLER_VELOCITY_THRESHOLD = AngularVelocity.ofBaseUnits(200.0, RotationsPerSecond);

    public static final double CLIMBER_REDUCTION = (144);

    public static CANcoderConfiguration encoderConfig() {
      final CANcoderConfiguration CFG = new CANcoderConfiguration();
      // MagnetSensor
      CFG.MagnetSensor.withSensorDirection(SensorDirectionValue.CounterClockwise_Positive);
      CFG.MagnetSensor.withMagnetOffset(Rotations.of(-0.13127 - 0.25).plus(Degrees.of(90.0)));
      CFG.MagnetSensor.withAbsoluteSensorDiscontinuityPoint(Rotations.of(1.0));
      return CFG;
    }

    public static TalonFXConfiguration rollerConfig() {
      final TalonFXConfiguration CFG = new TalonFXConfiguration();

      // MotorOutput
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Brake);

      // CurrentLimits
      CFG.CurrentLimits.withSupplyCurrentLimit(Amps.of(20.0));
      CFG.CurrentLimits.withSupplyCurrentLimitEnable(true);

      return CFG;
    }
    
    public static TalonFXConfiguration climberConfig() {
      final TalonFXConfiguration CFG = new TalonFXConfiguration();

      // MotorOutput
      CFG.MotorOutput.withNeutralMode(NeutralModeValue.Brake);

      // Slot0
      CFG.Slot0.withKP(400);
      CFG.Slot0.withKI(0.0);
      CFG.Slot0.withKD(0.01);

      CFG.MotionMagic.withMotionMagicAcceleration(600); // want more, like 900
      CFG.MotionMagic.withMotionMagicCruiseVelocity(100); // want more, like 200

      // Feedback
      CFG.Feedback.withFeedbackRemoteSensorID(ENCODER_ID);
      CFG.Feedback.withFeedbackSensorSource(FeedbackSensorSourceValue.FusedCANcoder);
      CFG.Feedback.withSensorToMechanismRatio(1.0);
      CFG.Feedback.withRotorToSensorRatio(CLIMBER_REDUCTION);

      // CurrentLimits
      CFG.CurrentLimits.withSupplyCurrentLimit(Amps.of(30.0));
      CFG.CurrentLimits.withSupplyCurrentLimitEnable(true);

      CFG.HardwareLimitSwitch.ReverseLimitSource = ReverseLimitSourceValue.LimitSwitchPin;
      CFG.HardwareLimitSwitch.ReverseLimitType = ReverseLimitTypeValue.NormallyClosed;
      CFG.HardwareLimitSwitch.ReverseLimitEnable = true;

      return CFG;
    }
  }

  public static class VisionConstants {
    public static final String LIMELIGHT_SCORING_NAME = "limelight-scoring";
    public static final String LIMELIGHT_INTAKE_NAME = "limelight-intake";

    public static final List<String> LIMELIGHTS =
        List.of(LIMELIGHT_SCORING_NAME, LIMELIGHT_INTAKE_NAME);
  }

  public static class LightsConstants {
    public static final int CANDLE_ID = 1;
    public static final String CANBUS_NAME = "canfd";
    public static final int OG = 7;
    public static final int SIDE_START = OG;
    public static final int SIDE_END = OG + 32;
    public static final int UPRIGHTS_START = SIDE_END;
    public static final int UPRIGHTS_END = UPRIGHTS_START + 22;
    public static final int LARSON_SIZE = 5;

    public static final RGBWColor DISABLED = new RGBWColor(255, 0, 0);

    public static final RGBWColor AUTO_DRIVING = new RGBWColor(0, 255, 255);

    public static final RGBWColor L1_COLOR = new RGBWColor(255, 255, 0);
    public static final RGBWColor L2_COLOR = new RGBWColor(0, 255, 255);
    public static final RGBWColor L3_COLOR = new RGBWColor(255, 255, 255);
    public static final RGBWColor L4_COLOR = new RGBWColor(160, 0, 255);
    public static final RGBWColor PROC_COLOR = new RGBWColor(0, 255, 0);
    public static final RGBWColor NET_COLOR = new RGBWColor(0, 0, 255);
    public static final RGBWColor CLIMB_COLOR = new RGBWColor(255, 0, 0);
    public static final RGBWColor COLLECT_COLOR = new RGBWColor(0, 255, 255);

    public static final double UP_SPEED = 50;
    public static final double DOWN_SPEED = 30;
    public static final double COLLECTED_SPEED = 60;
    public static final double CLIMB_CLIMB_SPEED = 80;
    public static final double IDLE_SPEED = 40;
    public static final double SCORE_STROBE_SPEED = 50;

    public static RGBWColor getScoreColor() {
      switch (Abomination.getScoreMode()) {
        case L1 -> {
          return L1_COLOR;
        }
        case L2 -> {
          return L2_COLOR;
        }
        case L3 -> {
          return L3_COLOR;
        }
        case L4 -> {
          return L4_COLOR;
        }
        case PROCESSOR -> {
          return PROC_COLOR;
        }
        case NET -> {
          return NET_COLOR;
        }
        case CLIMB -> {
          return CLIMB_COLOR;
        }
        default -> {
          return DISABLED;
        }
      }
    }
  }
}
