# TargetPositionHolder
Code written by Pranav Dharanipathy | 24133 | ChaiGPT Robotics
Documentation written by Aditya Bhadra | 24133 | ChaiGPT Robotics

## Installation

1. Download the repository as a ZIP file.
2. Extract the ZIP file
3. Copy the file into the "Teamcode" folder in your code
4. You're ready to begin! Look at the documentation provided as well as the example file in order to start using the library.

## Documentation

Creating a TargetPositionHolder object
```
private TargetPositionHolder positionHolder = new TargetPositionHolder();
```
Hold motor at position (Paramters without @NonNull are optional, use them for more control over how the motor is held!)
```
    public void holdDcMotor(@NonNull DcMotor motor, @NonNull double holdPosition, @NonNull VoltageSensor batteryVoltageSensor, double gearRatio, double ticksPerRev, double holdPower, double marginOfError, double powerMultiplier, double tuningPowerMultiplier)
```
Stop holding motor
```
public void stopHoldingDcMotor(DcMotor motor)
```
Full implementation
```
@Override
    public void loop() {
        robot.holdDcMotor(exampleMotor, 1000, batteryVoltageSensor);
        //holds motor for 1800 milliseconds then stops
        sleep(1800);

        robot.stopHoldingDcMotor(exampleMotor);

        sleep(1800);
        //some parameters are optional, use them to fine tune the holding pattern
        robot.holdDcMotor(exampleMotor, 1000, batteryVoltageSensor, "GEAR_RATIO", 2.5, "HOLD_POWER", 0.08);

        sleep(1800);

        robot.stopHoldingDcMotor(exampleMotor);

        sleep(1800);
    }
```

If you have any questions, email us at info@chaigptrobotics.org!
