package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.config.pedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.config.runmodes.Auto;
import org.firstinspires.ftc.teamcode.config.util.action.Actions;
import org.firstinspires.ftc.teamcode.config.util.action.SequentialAction;

@Autonomous(name="BlueBucket", group="B")
public class BlueBucket extends OpMode {
    public int pathState;
    public Auto auto;

    @Override
    public void init() {
        auto = new Auto(hardwareMap, telemetry, new Follower(hardwareMap), true, true);
    }

    //@Override
    /*public void init_loop() {
        auto.init_loop();
    }*/

    @Override
    public void start() {
        auto.start();
        setPathState(0);
    }

    @Override
    public void loop() {
        auto.update();
        pathUpdate();
    }

    public void pathUpdate() {
        switch (pathState) {
            case 0:
                auto.lift.toHighChamber.runAction();
                auto.follower.followPath(auto.preload);
                setPathState(1);
                setPathState(-1);
                break;
            case 1:
                if(!auto.follower.isBusy()) {

                    if (auto.lift.isAtTarget()) {
                        auto.lift.toHighChamberRelease.runAction();
                    }

                    if (auto.lift.isAtTarget()) {
                        auto.claw.openClaw.runAction();
                    }

                    auto.lift.toZero.runAction();
                    auto.follower.followPath(auto.element1);
                    auto.intake.pivotGround.runAction();
                    auto.intake.spinIn.runAction();
                }
                setPathState(2);
                break;
            case 2:
                if(!auto.follower.isBusy()) {
                    auto.follower.followPath(auto.score1);
                    auto.intake.spinStop.runAction();
                    auto.intake.pivotTransfer.runAction();
                }
                setPathState(3);
                break;
            case 3:
                if(!auto.follower.isBusy()) {
                    Actions.runBlocking(
                        new SequentialAction(
                            auto.transfer,
                            auto.lift.toLowBucket
                        )
                    );

                    if (auto.lift.isAtTarget()) {
                        auto.box.toScoring.runAction();
                    }

                    auto.follower.followPath(auto.element2);
                }
                setPathState(4);
                break;
            case 4:
                if(!auto.follower.isBusy()) {
                    auto.follower.followPath(auto.score2);
                }
                setPathState(5);
                break;
            case 5:
                if(!auto.follower.isBusy()) {
                    auto.follower.followPath(auto.element3);
                }
                setPathState(6);
                break;
            case 6:
                if(!auto.follower.isBusy()) {
                    auto.follower.followPath(auto.score3);
                }
                setPathState(7);
                break;
            case 7:
                if(!auto.follower.isBusy()) {
                    auto.follower.followPath(auto.park);
                }
                setPathState(-1);
                break;
        }
    }

    public void setPathState(int x) {
        pathState = x;
    }
}
