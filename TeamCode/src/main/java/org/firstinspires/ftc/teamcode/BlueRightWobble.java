package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="Blue_Right_Two_Wobble", group="Competition")
public class BlueRightWobble extends OpMode {

    /****************************
     *
     * Start the robot to the right of the blue start lines
     *
     ****************************/
    private int stateMachineFlow;
    MecanumDrive robot    = new MecanumDrive();
    Intake intake         = new Intake();
    Shooter shooter       = new Shooter();
    WobbleGrabber grabber = new WobbleGrabber();
    RingCamera    camera  = new RingCamera();

    RingNumber ringNumber = RingNumber.ZERO;

    //Can be used to choose whether we shoot power shots or in the high goal
    Boolean powerShot = true;

    //Choose shooter power-level
    boolean highVoltage = true;
    double power = -.825;
    double high  = -.9;

    private double waitTime;
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void init() {
        msStuckDetectInit = 11500;
        msStuckDetectLoop = 10000;

        robot.init(hardwareMap);
        intake.init(hardwareMap);
        shooter.init(hardwareMap);
        grabber.init(hardwareMap);
        camera.init(hardwareMap);
        robot.initIMU(hardwareMap);

        if (shooter.scalePowerShot() != Double.POSITIVE_INFINITY) {
            power = shooter.scalePowerShotDynamic();
            high = shooter.scaleHighGoalDynamic();
        }

        stateMachineFlow = 0;
    }
    public void init_loop() {
        //Choose whether or not we shoot power shots
        //input the relative change of the battery to determine the power of the shooter
        if (gamepad2.dpad_up) {
            powerShot = true;
        }if (gamepad2.dpad_down) {
            powerShot = false;
        }

        //This is a fail-safe in case the automatic adjuster doesn't work
        if (gamepad2.dpad_right) {
            power = -.825;
            high = -.9;
            highVoltage = true;
        }if (gamepad2.dpad_left) {
            power = -.875;
            high = -.93;
            highVoltage = false;
        }

        telemetry.addData("Volts", shooter.getBatteryVoltage());
        telemetry.addData("High Goal Power", high);
        telemetry.addData("Power Shot Power", power);
        telemetry.addData("Power Shot", powerShot);
        telemetry.update();
    }

    @Override
    public void loop() {

        if (shooter.scalePowerShot() != Double.POSITIVE_INFINITY) {
            power = shooter.scalePowerShotDynamic();
            high = shooter.scaleHighGoalDynamic();
        }
        telemetry.addData("Volts", shooter.getBatteryVoltage());
        telemetry.addData("High Goal Power", high);
        telemetry.addData("Power Shot Power", power);
        telemetry.addData("Case",stateMachineFlow);
        telemetry.update();

        switch(stateMachineFlow) {
            case 0:
                robot.resetAngle();
                //Drive into place to check number of rings
                robot.linearDrive(.45,-34);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                //intake.lowerIntake();
                stateMachineFlow++;
                break;
            case 1:
                //Use the camera to check for number of rings.
                if (camera.ringCount() == 1) {
                    ringNumber = RingNumber.ONE;
                } else if (camera.ringCount() == 4) {
                    ringNumber = RingNumber.FOUR;
                } else {
                    //Zero rings is our default case. In the event that the object recognition fails, we will assume zero.
                    ringNumber = RingNumber.ZERO;
                }
                stateMachineFlow++;
                break;
            case 2:
                //Use the value found by the camera scan to choose the path. The ZERO case is the default if the camera fails.
                if (ringNumber == RingNumber.ZERO) {
                    stateMachineFlow = 100;
                }else if (ringNumber == RingNumber.ONE) {
                    stateMachineFlow = 200;
                }else if (ringNumber == RingNumber.FOUR) {
                    stateMachineFlow = 300;
                }
                camera.decativate();
                telemetry.addData("Case",stateMachineFlow);
                break;

            /***************************
             *
             * Zero Rings on the Field
             *
              **************************/
            case 100:
                //Drive forward
                robot.linearDrive(.5,-42);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 101:
                //Drive left into zone A
                robot.sideDrive(.4,37);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 102:
                //Lower Wobble Grabber
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 103:
                //Release wobble goal
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 104:
                //Back up behind shot line
                robot.linearDrive(.5,16);
                stateMachineFlow++;
                break;
            case 105:
                grabber.gripWrist.setPosition(.23);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 106:
                /*
                High goal or powershot
                 */
                if (!powerShot) {
                    //High goal
                    stateMachineFlow++;
                } else {
                    //powershot
                    stateMachineFlow = 150;
                }
                telemetry.addData("Case",stateMachineFlow);
                break;
            case 107:
                //Move right to be in line with goal
                robot.sideDrive(.4, -13);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 108:
                //Turn on shooter
                shooter.shooterPower(high);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 109:
                //Shoot rings into the goal
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 110:
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                shooter.shooterPower(0);
                stateMachineFlow = 400;
                break;
                /*
                to case 400
                 */

            case 150:
                //Move right in line with first power shot
                robot.gStatTurn(.2,-robot.getAngle());
                robot.sideDrive(.4,-38);
                stateMachineFlow++;
                break;
            case 151:
                //Turn on the shooter
                shooter.shooterPower(power);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 152:
                //Shoot first power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                intake.intakePower(0);
                stateMachineFlow++;
                break;
            case 153:
                //Move right to second power shot
                robot.sideDrive(.4,-7);
                //robot.gStatTurn(.2,-robot.getAngle());
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 154:
                //Shoot second power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 155:
                //Turn off shooter and intake
                shooter.shooterPower(0);
                intake.intakePower(0);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow = 401;
                break;
                /*
                to case 401
                 */

            /***************************
             *
             * One Ring on the Field
             *
             **************************/
            case 200:
                //Drive forward to zone B
                robot.linearDrive(.5,-60);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 201:
                robot.sideDrive(.4,12);
                //Lower wobble grabber
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 202:
                //Release wobble goal
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 203:
                //Back up behind shot line
                robot.linearDrive(.5,35);
                stateMachineFlow++;
                break;
            case 204:
                //Raise grabber
                grabber.gripWrist.setPosition(.23);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 205:
                /*
                High goal or powershot
                 */
                if (!powerShot) {
                    //High goal
                    stateMachineFlow++;
                } else {
                    //powershot
                    stateMachineFlow = 250;
                }
                telemetry.addData("Case",stateMachineFlow);
                break;
            case 206:
                //Move to be in line with goal
                robot.sideDrive(.4,20);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 207:
                //Turn on shooter
                shooter.shooterPower(high);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 208:
                //Shoot rings into the goal
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 209:
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                shooter.shooterPower(0);
                stateMachineFlow = 400;
                break;
                /*
                to case 400
                 */

            case 250:
                //Move right in line with first power shot
                robot.sideDrive(.4,-14);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 251:
                //Turn on the shooter
                shooter.shooterPower(power);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 252:
                //Shoot first power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                intake.intakePower(0);
                stateMachineFlow++;
                break;
            case 253:
                //Move right to second power shot
                robot.sideDrive(.4,-7);
                //robot.gStatTurn(.2,-robot.getAngle());
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 254:
                //Shoot second power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 255:
                //Turn off shooter and intake
                intake.intakePower(0);
                shooter.shooterPower(0);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow = 401;
                break;
                /*
                to case 401
                 */

                /***************************
                 *
                 * Four Rings on the Field
                 *
                 **************************/
            case 300:
                //Drive forward
                robot.linearDrive(.6,-84);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 301:
                //Drive to zone C
                robot.sideDrive(.4,37);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 302:
                //Lower Wobble Grabber
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 303:
                //Release wobble goal
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 304:
                //Back up behind shot line
                robot.linearDrive(.65,56);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 305:
                grabber.gripWrist.setPosition(.23);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 306:
                /*
                High goal or powershot
                 */
                if (!powerShot) {
                    //High goal
                    stateMachineFlow++;
                } else {
                    //powershot
                    stateMachineFlow = 350;
                }
                telemetry.addData("Case",stateMachineFlow);
                break;
            case 307:
                //Move right to be in line with goal
                robot.sideDrive(.4,-13);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 308:
                //Turn on shooter
                shooter.shooterPower(high);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 309:
                //Shoot rings into the goal
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                stateMachineFlow++;
                break;
            case 310:
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(high);
                }
                intake.intakePower(0);
                shooter.shooterPower(0);
                stateMachineFlow = 400;
                break;
                /*
                to 400
                 */

            case 350:
                //Move right in line with first power shot
                robot.gStatTurn(.2,-robot.getAngle());
                robot.sideDrive(.4,-37);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 351:
                //Turn on the shooter
                shooter.shooterPower(power);
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 352:
                //Shoot first power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                intake.intakePower(0);
                stateMachineFlow++;
                break;
            case 353:
                //Move right to second power shot
                robot.sideDrive(.4,-7);
                //robot.gStatTurn(.2,-robot.getAngle());
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 354:
                //Shoot second power shot
                intake.intakePower(1);
                waitTime = .5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {
                    shooter.shooterPower(power);
                }
                stateMachineFlow++;
                break;
            case 355:
                //Turn off shooter and intake
                intake.intakePower(0);
                shooter.shooterPower(0);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 356:
                robot.linearDrive(.6, -5);
                stateMachineFlow++;
                break;
                /*
                to 401
                 */

            /***************************
             *
             * Getting second wobble
             *
             **************************/
            case 400:
                //move robot from high goal path to end position of power shot path
                robot.sideDrive(.4,-25);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 401:
                //move robot from end position of power shot back towards the wall
                //robot.gStatTurn(.2,-robot.getAngle());
                robot.linearDrive(.7,45);
               // robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 402:
                //lower the grabber
                grabber.lowerGripper();
                stateMachineFlow++;
                break;
            case 403:
                //move to the wobble
                robot.sideDrive(.3,32);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 404:
                //close the grabber
                grabber.gripperPosition(.77);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 405:
                //Lift the wobble goal
                grabber.gripWrist.setPosition(.23);
                waitTime = 1.5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 406:
                //Use the value found by the camera scan to choose the path. The ZERO case is the default if the camera fails.
                if (ringNumber == RingNumber.ZERO) {
                    stateMachineFlow = 500;
                }else if (ringNumber == RingNumber.ONE) {
                    stateMachineFlow = 550;
                }else if (ringNumber == RingNumber.FOUR) {
                    stateMachineFlow = 600;
                }
                break;

                /***************************
                 *
                 * Zero Rings on the Field
                 *
                 **************************/
            case 500:
                //move forward to zone A
                robot.linearDrive(.5,-61);
                stateMachineFlow++;
                break;
            case 501:
                robot.sideDrive(.4,8);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 502:
                //lower the wobble
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 503:
                //let go of the wobble
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 504:
                //move away from the wobble
                robot.sideDrive(.4,-5);
                //robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 505:
                //raise wobble arm
                grabber.gripWrist.setPosition(.23);
                waitTime = 1.5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;

            /***************************
             *
             * One Ring on the Field
             *
             **************************/
            case 550:
                robot.sideDrive(.4,-26);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 551:
                robot.linearDrive(.6,-81);
                stateMachineFlow++;
                break;
            case 552:
                robot.sideDrive(.4,11);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 553:
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 554:
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 555:
                robot.sideDrive(.4,-6);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 556:
                robot.linearDrive(.65,20);
                grabber.gripWrist.setPosition(.23);
                waitTime = 1.5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;

            /***************************
             *
             * Four Rings on the Field
             *
             **************************/
            case 600:
                robot.sideDrive(.4,-26);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 601:
                robot.linearDrive(1,-102);
                stateMachineFlow++;
                break;
            case 602:
                robot.sideDrive(.4,32);
                robot.gStatTurn(.2,-robot.getAngle());
                stateMachineFlow++;
                break;
            case 603:
                grabber.lowerGripper();
                waitTime = 1;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 604:
                grabber.gripperPosition(0);
                waitTime = .25;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;
            case 605:
                robot.linearDrive(1, 44);
                stateMachineFlow++;
                break;
            case 606:
                grabber.gripWrist.setPosition(.23);
                waitTime = 1.5;
                runtime.reset();
                time = runtime.time();
                while (waitTime > runtime.time() - time) {

                }
                stateMachineFlow++;
                break;

            default:
                //End program...
                stop();
                break;
        }
    }
}
