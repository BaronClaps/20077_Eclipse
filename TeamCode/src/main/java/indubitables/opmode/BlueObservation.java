package indubitables.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import indubitables.pedroPathing.follower.Follower;
import indubitables.config.runmodes.Auto;
import indubitables.config.util.action.Actions;
import indubitables.config.util.action.SequentialAction;
import indubitables.pedroPathing.util.Timer;

@Autonomous(name="BlueObservation", group="A")
public class BlueObservation extends OpMode {
    public int pathState;
    public Auto auto;

    public Timer pathTimer = new Timer();

    @Override
    public void init() {
        auto = new Auto(hardwareMap, telemetry, new Follower(hardwareMap), true, false);

        telemetry.addData("State: ", pathState);
        telemetry.addData("X: ", auto.follower.getPose().getX());
        telemetry.addData("Y: ", auto.follower.getPose().getY());
        telemetry.addData("Heading: ", auto.follower.getPose().getHeading());
        telemetry.addData("Action Busy?: ", auto.actionBusy);
        telemetry.update();
    }

    @Override
    public void start() {
        auto.start();
        setPathState(0);
    }

    @Override
    public void loop() {
        auto.update();
        pathUpdate();

        telemetry.addData("State: ", pathState);
        telemetry.addData("X: ", auto.follower.getPose().getX());
        telemetry.addData("Y: ", auto.follower.getPose().getY());
        telemetry.addData("Heading: ", auto.follower.getPose().getHeading());
        telemetry.addData("Action Busy?: ", auto.actionBusy);
        telemetry.update();
    }

    public void pathUpdate() {
        switch (pathState) {
            case 0: //Runs to the position of the preload and holds it's point at 0.5 power
                auto.extend.toZero();
                auto.follower.setMaxPower(0.5);
                auto.follower.followPath(auto.preload, true);
                setPathState(1);
                break;
            case 1: //Waits until follower reaches it's position then begins the Chamber State Machine
                if(auto.follower.getPose().getX() > auto.preloadPose.getX()) {
                    auto.startChamber();
                    setPathState(2); }
                break;
            case 2: //Once Chamber State Machine Machine finishes, begins Pathchain to push elements to the submersible
                if(auto.actionNotBusy()) {
                    auto.follower.setMaxPower(0.5);
                    auto.follower.followPath(auto.pushSamples, true);
                    setPathState(3); }
                break;
            case 3: //Waits until follower reaches it's position then begins the Specimen State Machine
                if(!auto.follower.isBusy()) {
                    auto.startSpecimen();
                    auto.liftPIDF = false;
                    auto.lift.manual(-0.7);
                    setPathState(10001); }
                break;
            case 10001:
                if(pathTimer.getElapsedTimeSeconds() > 0.5) {
                    auto.lift.manual(0);
                    setPathState(4); }
                break;
            case 4: //Runs to the position of the grab1 and holds it's point at full power
                if(auto.actionNotBusy() && !auto.follower.isBusy()) {
                    auto.follower.setMaxPower(0.65);
                    auto.follower.followPath(auto.grab1, true);
                    setPathState(5); }
                break;
            case 5: //Closes the claw when the follower reaches the grab1 position
                if(pathTimer.getElapsedTimeSeconds() > 1.5) {
                    auto.claw.close();
                    setPathState(6); }
                break;
            case 6: //Sets the arm to a neutral position and puts lifts to zero;
                if(pathTimer.getElapsedTimeSeconds() > 0.25) {
                    auto.init();
                    auto.follower.setMaxPower(0.9);
                    auto.follower.followPath(auto.specimen1, true);
                    setPathState(8); }
                break;
            case 8: //Waits until follower reaches it's position then begins the Chamber State Machine
                if(pathTimer.getElapsedTimeSeconds() > 0.5) {
                    auto.startChamber2();
                    setPathState(9); }
                break;
            case 9: //Resets the lifts and starts the Specimen State Machine
                if(auto.actionNotBusy()) {
                    auto.startSpecimen();
                    setPathState(10); }
                break;
            case 10: //Begins the path for grab 2 & closes the claw once it reaches position and passes 0.75 seconds
                if(pathTimer.getElapsedTimeSeconds() > 0) {
                    auto.follower.setMaxPower(0.5);
                    auto.follower.followPath(auto.grab2, true);
                    setPathState(11);
                }
                break;
            case 11:
                    if(pathTimer.getElapsedTimeSeconds() > 3.5) {
                        auto.claw.close();
                        setPathState(12);
                    }
                break;
            case 12: //Waits 0.25 seconds and puts robot in neutral position
                if(pathTimer.getElapsedTimeSeconds() > 0.25) {
                    auto.init();
                    setPathState(13); }
                break;
            case 13: //Drives to chamber once action finishes
                    auto.follower.setMaxPower(0.9);
                    auto.follower.followPath(auto.specimen2, true);
                    setPathState(14);
                break;
            case 14: //Starts the Chamber State Machine
                if(pathTimer.getElapsedTimeSeconds() > 1.25) {
                    auto.startChamber2();
                    setPathState(15); 
                }
                break;
            case 15: //Park and End the autonomous
                if(auto.actionNotBusy()) {
                    auto.follower.followPath(auto.park, true);
                    setPathState(-1); 
                }
                break;
        }
    }

    public void setPathState(int x) {
        pathState = x;
        pathTimer.resetTimer();
    }
}
