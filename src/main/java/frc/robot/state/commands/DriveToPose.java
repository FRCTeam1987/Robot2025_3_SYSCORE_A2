package frc.robot.state.commands;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static frc.robot.RobotContainer.JOYSTICK;
import static frc.robot.RobotContainer.MAX_SPEED;

import com.ctre.phoenix6.swerve.SwerveRequest;
import dev.doglog.DogLog;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.RobotContainer;
import frc.robot.state.Abomination;
import frc.robot.state.logic.mode.ScoreMode;
import frc.robot.subsystems.Drivetrain;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DriveToPose extends Command {
  private final Drivetrain DRIVE = RobotContainer.DRIVETRAIN;
  private final Supplier<Pose2d> TARGET;
  private final Supplier<Pose2d> ROBOT;
  private final HolonomicDriveController HOLONOMIC =
      new HolonomicDriveController(
          new PIDController(4.625, 0, 0.0001),
          new PIDController(4.625, 0, 0.0001),
          new ProfiledPIDController(
              12.0,
              0,
              0,
              new TrapezoidProfile.Constraints(Units.degreesToRadians(360.0), 8.0),
              0.02));
  boolean running = false;
  private Debouncer hasTargetDebounce;
  private BooleanSupplier freeY = () -> false;

  public DriveToPose(Pose2d target) {
    addRequirements(DRIVE);
    this.TARGET = () -> target;
    this.ROBOT = () -> RobotContainer.DRIVETRAIN.getState().Pose;
  }

  public DriveToPose(Supplier<Pose2d> target) {
    addRequirements(DRIVE);
    this.TARGET = target;
    this.ROBOT = () -> RobotContainer.DRIVETRAIN.getState().Pose;
  }

  public DriveToPose(Supplier<Pose2d> target, BooleanSupplier freeY) {
    addRequirements(DRIVE);
    this.TARGET = target;
    this.ROBOT = () -> RobotContainer.DRIVETRAIN.getState().Pose;
    this.freeY = freeY;
  }

  @Override
  public void initialize() {
    this.hasTargetDebounce = new Debouncer(0.06);
    Pose2d currentPose = ROBOT.get();

    ChassisSpeeds fieldVelocity = RobotContainer.DRIVETRAIN.getState().Speeds;
    Translation2d linearFieldVelocity =
        new Translation2d(fieldVelocity.vx, fieldVelocity.vy);

    HOLONOMIC.getXController().reset();
    HOLONOMIC.getYController().reset();
    HOLONOMIC.getThetaController().reset(currentPose.getRotation().getRadians());
  }

  @Override
  public void execute() {
    running = true;

    Pose2d currentPose = ROBOT.get();
    Pose2d targetPose = TARGET.get();

    if (freeY.getAsBoolean()) {
      ChassisSpeeds speeds =
              HOLONOMIC.calculate(currentPose, targetPose, 0.0, targetPose.getRotation()).toRobotRelative(new Rotation2d());
      DRIVE.setControl(
          new SwerveRequest.ApplyRobotSpeeds()
              .withSpeeds(
                  new ChassisSpeeds(
                      speeds.vx,
                      MAX_SPEED
                          .times(
                              Abomination.getScoreMode() == ScoreMode.CLIMB
                                  ? JOYSTICK.getLeftY()
                                  : -JOYSTICK.getLeftX())
                          .in(MetersPerSecond),
                      speeds.omega)));
    } else {
      DRIVE.setControl(
          new SwerveRequest.ApplyRobotSpeeds()
              .withSpeeds(
                      HOLONOMIC.calculate(currentPose, targetPose, 0.0, targetPose.getRotation()).toRobotRelative(new Rotation2d())));
    }

    DogLog.log("DriveToPose/targetPose", targetPose);
    DogLog.log("DriveToPose/isRunning", running);
  }

  @Override
  public void end(boolean interrupted) {
    running = false;
  }

  @Override
  public boolean isFinished() {
    return atGoal();
  }

  public boolean atGoal() {
    final Pose2d pose = DRIVE.getState().Pose;
    return hasTargetDebounce.calculate(
        running
            && pose.getTranslation().getDistance(TARGET.get().getTranslation()) < 0.036
            && pose.getRotation()
                .getMeasure()
                .isNear(TARGET.get().getRotation().getMeasure(), Degrees.of(1.5))
            && DRIVE.getState().Speeds.vx < 0.03
            && DRIVE.getState().Speeds.vy < 0.03);
  }
}
