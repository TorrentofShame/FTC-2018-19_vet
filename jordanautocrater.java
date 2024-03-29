package org.firstinspires.ftc.teamcode;

import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.DogeCV;
import com.disnodeteam.dogecv.detectors.roverrukus.GoldAlignDetector;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;


@Autonomous(name="New Crater", group="Scheduled")
//@Disabled
public class jordanautocrater extends LinearOpMode {




    private DcMotor leftFront = null;
    private DcMotor leftBack = null;
    private DcMotor rightFront = null;
    private DcMotor rightBack = null;
    private DcMotor armRight = null;
    private DcMotor armLeft = null;
    private Servo flag;
    private int time;
    private boolean seenGold = false;
    double globalAngle;
    int position = 0;

    // The IMU sensor object
    BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;
    Orientation             lastAngles = new Orientation();

    double angle;

    static final double     TETRIX_COUNTS_PER_MOTOR_REV         = 1440 ;    // eg: TETRIX Motor Encoder
    private static final double PROPULSION_DRIVE_GEAR_REDUCTION = 1.0;       // This is < 1.0 if geared UP
    private static final double WHEEL_CIRCUMFERENCE             = 4.0 * 3.14159;    // For figuring circumference
    private static final double PROPULSION_COUNTS_PER_INCH = (TETRIX_COUNTS_PER_MOTOR_REV * PROPULSION_DRIVE_GEAR_REDUCTION) / WHEEL_CIRCUMFERENCE;




    private GoldAlignDetector detector;

    private ElapsedTime runtime = new ElapsedTime();


    @Override
    public void runOpMode() {




        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack  = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        armRight = hardwareMap.get(DcMotor.class, "armRight");
        armLeft = hardwareMap.get(DcMotor.class, "armLeft");
        flag = hardwareMap.get(Servo.class, "flag");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);

        armRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        telemetry.addData("Status", "DogeCV 2018.0 - Gold Align Example");


        detector = new GoldAlignDetector();
        detector.init(hardwareMap.appContext, CameraViewDisplay.getInstance());
        detector.useDefaults();


        // Optional Tuning
        detector.alignSize = 250; // How wide (in pixels) is the range in which the gold object will be aligned. (Represented by green bars in the preview)
        detector.alignPosOffset = 0; // How far from center frame to offset this alignment zone.
        detector.downscale = 0.4; // How much to downscale the input frames

        detector.areaScoringMethod = DogeCV.AreaScoringMethod.MAX_AREA; // Can also be PERFECT_AREA
        //detector.perfectAreaScorer.perfectArea = 10000; // if using PERFECT_AREA scoring
        detector.maxAreaScorer.weight = 0.005;

        detector.ratioScorer.weight = 5;
        detector.ratioScorer.perfectRatio = 1.0;

        detector.enable();

        imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);


        //Start code
        waitForStart();

        //Put robot on ground
        arm(.8, -40);
        sleep(500);
        resetAngle();
//        //Get robot.
//   out of hook
        motors(.2,-.2,-.2,.2);
        sleep(500);
        //Check Middle
        detector.alignPosOffset = 0;
        sleep(700);
        if(detector.getAligned()) {
            //Go middle
            telemetry.addData("seen","right");
            telemetry.update();
            //Forward
            position=2;
            forward(0.7,12);
            sleep(100);
            //Allign
            motors(-.5,.5,.5,-.5);
            sleep(100);
            sleep(300);
            if(detector.getAligned()) {
                gold();
            }else{
                //Allign to right if middle is not seen
                motors(-.5,.5,.5,-.5);
                sleep(1600);
                sleep(300);
                if(detector.getAligned()){
                    position= 3;
                    gold();
                }else{
                    position=1;
                    // Allign To Left if Middle is not then Right is Not
                    motors(.5,-.5,-.5,.5);
                    sleep(2600);
                    sleep(300);
                    gold();
                }
            }
        }
          else if(!detector.getAligned()){
            detector.alignPosOffset = 220;
            sleep(500);
            //check Right
        }
        if(detector.getAligned() && !seenGold){
            position=3;

            //Go Right
            //Forward
            forward(0.7,10);
            rotateRight(0.2,-2);
            sleep(100);
            //Allign
            motors(-.5,.5,.5,-.5);
            sleep(1600);
            sleep(300);
            //Check Right
            if(detector.getAligned()){
                position = 3;
                gold();

            }else{
                //Check Middle
                //Allign to Middle
                motors(.5,-.5,-.5,.5);
                sleep(1600);
                sleep(300);
                if(detector.getAligned()){
                    position = 2;
                    gold();
                }else{
                    position = 1;
                    //Allign to Left
                    motors(.5,-.5,-.5,.5);
                    sleep(1600);
                    sleep(400);
                    gold();
                }
            }

        }else if(!detector.getAligned() && !seenGold){
            position= 1;
            //go left
            //Forward
            forward(0.7,10);
            rotateRight(0.2,-2);
            sleep(100);
            //Allign
            motors(.5,-.5,-.5,.5);
            sleep(1300);
            sleep(300);
            if(detector.getAligned()){
                //Left Gold
                gold();
            }else{
                //Allign to Middle
                motors(-.5,.5,.5,-.5);
                sleep(1600);
                sleep(300);
                if(detector.getAligned()){
                    position = 2;
                    gold();
                }else{
                    position=3;
                    //Allign to Right
                    motors(-.5,.5,.5,-.5);
                    sleep(1600);
                    sleep(300);
                    gold();
                }
            }

        }



        telemetry.addData("time:", time);
        telemetry.update();

        //move left
        motors(0.6, -0.6, -0.6, 0.6);
        sleep(time);
        //Turn Toward The Wall
        rotateRight(.8,47);
        sleep(600);
        //Move towards wall
        forward(.7,6);
        //Allign
        sleep(300);
//        forward(.7,1);
//        sleep(300);
        //Move to flag corner
        motors(.6, -.6, -.6, .6);
        sleep(4000);
        sleep(500);
        //Drop flag
        flag.setPosition(0);
        sleep(500);
        flag.setPosition(1);
        //Move away from flag corner
        motors(-0.6, 0.6, 0.6, -0.6);
        sleep(2200);
        //allign to crater
        rotateRight(0.5,-45);
        sleep(300);
        forward(.6,12);
        sleep(200);
        arm(.8, -73);
        sleep(500);
    while (opModeIsActive()) {
        }
    }


    public void sleep(int time){
        try{
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        stopMotors();
    }

    public void motors(double rf, double lf, double rb, double lb){
        leftFront.setPower(lf);
        leftBack.setPower(lb);
        rightFront.setPower(rf);
        rightBack.setPower(rb);
    }

    public void stopMotors(){
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
    }

    private void resetAngle()
    {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    private double getAngle()
    {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    public void arm (double power, int position){
        //positive goes back, negative goes forward

        int inches = (int) (position * PROPULSION_COUNTS_PER_INCH);

        armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armRight.setTargetPosition(inches);
        armLeft.setTargetPosition(inches);

        armRight.setPower(power);
        armLeft.setPower(power);

        while(armRight.isBusy()  && armLeft.isBusy()){
            telemetry.addData("Current position",armRight.getCurrentPosition());
            telemetry.addData("Position to Go",inches);
            telemetry.update();
        }
        armRight.setPower(0);
        armLeft.setPower(0);
    }
    public void gold(){
        //move forward To Gold
        motors(.8,.8,.8,.8);
        sleep(350);
        sleep(200);
        //move back From Gold
        motors(-.8,-.8,-.8,-.8);
        sleep(450);
        time = (position-1) * 1100 + 800;
        seenGold = true;
    }
    private void forward(double power, int dist){

        int inches = (int) (dist * PROPULSION_COUNTS_PER_INCH);

        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        telemetry.addData("git", "gud");
        telemetry.update();

        rightFront.setTargetPosition(inches);

        if(dist > 0){motors(power+0.1, power, power+0.1, power);}
        if(dist < 0){motors(power-0.1, -power, -power-0.1, -power);}

        while(rightFront.isBusy()){
        }

        stopMotors();

        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

    }

    private void rotateRight(double power, int ang){

        if(ang > getAngle()){
            motors(power,-power,power,-power);
            while(getAngle() < ang){            }
            while(getAngle() > ang){
                motors(-.2,.2,-.2,.2);
            }

        }else if(ang < getAngle()){
            motors(-power,power,-power,power);
            while(getAngle() > ang){            }
            while(getAngle() < ang){
                motors(.2,-.2,.2,-.2);
            }
        }
        else{
            while(getAngle() > 0){
                motors(-.2,.2,-.2,.2);
            }
            while(getAngle() < 0){
                motors(.2,-.2,.2,-.2);
            }
        }


        stopMotors();
        telemetry.addData("angle to go" + ang, "actual angle: "+getAngle());
        telemetry.update();

    }


    /**
     * Perform one cycle of closed loop heading control.
     *
     * @param speed     Desired speed of turn.
     * @param angle     Absolute Angle (in Degrees) relative to last gyro reset.
     *                  0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                  If a relative angle is required, add/subtract from current heading.
     * @param PCoeff    Proportional Gain coefficient
     * @return
     */

}