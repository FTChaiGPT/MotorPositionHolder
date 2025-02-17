package org.firstinspires.ftc.teamcode.DriveAdditionalActions;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Autonomous (name = "TargetPositionHolder")
public class TargetPositionHolder extends LinearOpMode {
    public void runOpMode() {/*--EMPTY--*/}

    private volatile boolean isMotorHolding = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private double power;

    public static double TRAINED_TICKS_PER_REV = 537.7;

    public static double LOWER_MAX_POWER_LMARGIN = 2500;
    public static double HALF_POWER_LMARGIN = 1200;
    public static double MOD_POWER_LMARGIN = 900;
    public static double LOW_POWER_LMARGIN = 500;
    public static double MIN_POWER_LMARGIN = 35;

    private volatile double TICKS_PER_REV = 537.7;
    public volatile double gearRatio = 1; /** GEAR_RATIO output (motor) speed / input (motor) speed **/
    public volatile double holdPower = 0; /** power set once motor has reached its TargetPosition **/
    public volatile double marginOfError = 5 ;
    public volatile double powerMultiplier = 1;
    public volatile double tuningPowerMultiplier = 1; /** made on top of power multiplier adjustments **/

    private volatile Map<DcMotor, Future<?>> motorTasks = new HashMap<>(); /** HashMap to create instances of classes **/ //holds all Futures of the ExecutorService
    /** Future is used to handle specific parts of the ExecutorService instead of only handling all or none **/


    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev, double holdPower, double marginOfError, double powerMultiplier, double tuningPowerMultiplier) {

        this.gearRatio = gearRatio;
        TICKS_PER_REV = ticksPerRev;
        this.holdPower = holdPower;
        this.marginOfError = marginOfError;
        this.powerMultiplier = powerMultiplier;
        this.tuningPowerMultiplier = tuningPowerMultiplier;

        double MAX_POWER = Math.min(1 / gearRatio, 1);
        /** ternary operator **/
        double LOWER_MAX_POWER = ((MAX_POWER / 1.3) * powerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : ((MAX_POWER / 1.3) * powerMultiplier);
        double HALF_POWER = ((MAX_POWER / 5.5) * powerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : ((MAX_POWER / 2) * powerMultiplier);
        double MOD_POW = ((MAX_POWER / 7.5) * powerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : ((MAX_POWER / 3.5) * powerMultiplier);
        double LOW_POW = ((MAX_POWER / 9) * powerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : ((MAX_POWER / 8) * powerMultiplier);
        double MIN_POW = ((MAX_POWER / 10) * powerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : ((MAX_POWER / 9.5) * powerMultiplier);
        double LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POW = ((MAX_POWER / 5) * powerMultiplier) > 1 ? 1 : ((MAX_POWER / 7.5) * powerMultiplier);

        /** tuningPowerMultiplier is applied after powerMultiplier is applied **/
        double MOD_POWER = (MOD_POW * tuningPowerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : (MOD_POW * tuningPowerMultiplier);
        double LOW_POWER = (LOW_POW * tuningPowerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : (LOW_POW * tuningPowerMultiplier);
        double MIN_POWER = (MIN_POW * tuningPowerMultiplier) > 1 ? ((1 * powerMultiplier <= 1) ? 1 : (1 * powerMultiplier)) : (MIN_POW * tuningPowerMultiplier);
        double LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POWER = (LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POW * tuningPowerMultiplier) > 1 ? 1 : (LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POW * tuningPowerMultiplier);


        isMotorHolding = true;

        Future<?> motorTask = executor.submit(() -> {

            motor.setTargetPosition((int) holdPosition);

            try {
                while (isMotorHolding) {

                    power = setAppropriatePower(MAX_POWER, LOWER_MAX_POWER, HALF_POWER, MOD_POWER, LOW_POWER, MIN_POWER, LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POWER, batteryVoltageSensor, motor);

                    if (Math.floor(Math.abs(motor.getCurrentPosition())) > (Math.abs(motor.getTargetPosition()) - marginOfError) && Math.floor(Math.abs(motor.getCurrentPosition())) < (Math.abs(motor.getTargetPosition()) + marginOfError)) {
                        motor.setPower(holdPower);
                    } else {
                        motor.setPower(power);
                        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    }
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            } finally { clearFutureOfDcMotor(motor); }

        });
        if (!motorTasks.containsKey(motor)) {
            motorTasks.put(motor, motorTask); //motor task is added to MotorTasks
        }

    }
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, TICKS_PER_REV, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, TICKS_PER_REV, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, ticksPerRev, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev, double holdPower){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, ticksPerRev, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev, double holdPower, double marginOfError, double powerMultiplier){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, ticksPerRev, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev, double holdPower, double marginOfError){
        holdDcMotor(motor, holdPosition, batteryVoltageSensor, gearRatio, ticksPerRev, holdPower, marginOfError, powerMultiplier, tuningPowerMultiplier);
    }
    public void clearFutureOfDcMotor(DcMotor motor) {
        if (motorTasks.containsKey(motor)) motorTasks.remove(motor);
    }

    public void killExecutor() {
        for (DcMotor motor : motorTasks.keySet()) stopHoldingDcMotor(motor);

        executor.shutdown();
    }

    public void stopHoldingDcMotor(DcMotor motor) {
        Future<?> motorTask = motorTasks.get(motor);
        if (motorTask != null) {
            motorTask.cancel(true);
        }
        isMotorHolding = false;
        motor.setPower(0.0);
    }

    public double setAppropriatePower(double MAX_POWER,
                                      double LOWER_MAX_POWER,
                                      double HALF_POWER,
                                      double MOD_POWER,
            /* gets motor powers */      double LOW_POWER,
                                      double MIN_POWER,
                                      double LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POWER,
                                      VoltageSensor batteryVoltageSensor,
                                      DcMotor motor)
        /* start of loop >>> */ {
        double powerAmount;
        double positionDifference = Math.abs(motor.getTargetPosition() - motor.getCurrentPosition());
        double currentBatteryVoltage = batteryVoltageSensor.getVoltage();
        //evaluates motor powers
        if (positionDifference <= TICKS_PER_REV * (MIN_POWER_LMARGIN / TRAINED_TICKS_PER_REV)) {
            if (currentBatteryVoltage <= 11.75) {
                powerAmount = LOW_POWER;
            }
            else if (currentBatteryVoltage <= 12) {
                powerAmount = LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POWER;
            }
            else powerAmount = MIN_POWER;
        } else if (positionDifference <= TICKS_PER_REV * (LOW_POWER_LMARGIN / TRAINED_TICKS_PER_REV)) {
            if (currentBatteryVoltage <= 11.75) {
                powerAmount = LOW_BATTERY_VOLTAGE_LOW_AND_MIN_POWER;
            }
            else powerAmount = LOW_POWER;
        } else if (positionDifference <= TICKS_PER_REV * (MOD_POWER_LMARGIN / TRAINED_TICKS_PER_REV)) {
            powerAmount = MOD_POWER;
        } else if (positionDifference <= TICKS_PER_REV * (HALF_POWER_LMARGIN / TRAINED_TICKS_PER_REV)) {
            powerAmount = HALF_POWER;
        } else if (positionDifference <= TICKS_PER_REV * (LOWER_MAX_POWER_LMARGIN / TRAINED_TICKS_PER_REV)) {
            powerAmount = LOWER_MAX_POWER;
        }
        else powerAmount = MAX_POWER;

        return powerAmount;
    }
}
