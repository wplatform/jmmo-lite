package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "vehicle_seat")
@Db2DataBind(name = "VehicleSeat.db2", layoutHash = 0x242E0ECD, fields = {
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flagsB", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flagsC", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"attachmentOffsetX", "attachmentOffsetY", "attachmentOffsetZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "enterPreDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "enterSpeed", type = Db2Type.FLOAT),
        @Db2Field(name = "enterGravity", type = Db2Type.FLOAT),
        @Db2Field(name = "enterMinDuration", type = Db2Type.FLOAT),
        @Db2Field(name = "enterMaxDuration", type = Db2Type.FLOAT),
        @Db2Field(name = "enterMinArcHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "enterMaxArcHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "exitPreDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "exitSpeed", type = Db2Type.FLOAT),
        @Db2Field(name = "exitGravity", type = Db2Type.FLOAT),
        @Db2Field(name = "exitMinDuration", type = Db2Type.FLOAT),
        @Db2Field(name = "exitMaxDuration", type = Db2Type.FLOAT),
        @Db2Field(name = "exitMinArcHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "exitMaxArcHeight", type = Db2Type.FLOAT),
        @Db2Field(name = "passengerYaw", type = Db2Type.FLOAT),
        @Db2Field(name = "passengerPitch", type = Db2Type.FLOAT),
        @Db2Field(name = "passengerRoll", type = Db2Type.FLOAT),
        @Db2Field(name = "vehicleEnterAnimDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "vehicleExitAnimDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraEnteringDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraEnteringDuration", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraExitingDelay", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraExitingDuration", type = Db2Type.FLOAT),
        @Db2Field(name = {"cameraOffsetX", "cameraOffsetY", "cameraOffsetZ"}, type = Db2Type.FLOAT),
        @Db2Field(name = "cameraPosChaseRate", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraFacingChaseRate", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraEnteringZoom", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraSeatZoomMin", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraSeatZoomMax", type = Db2Type.FLOAT),
        @Db2Field(name = "uiSkinFileDataID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "enterAnimStart", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "enterAnimLoop", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "rideAnimStart", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "rideAnimLoop", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "rideUpperAnimStart", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "rideUpperAnimLoop", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "exitAnimStart", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "exitAnimLoop", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "exitAnimEnd", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleEnterAnim", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleExitAnim", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleRideAnimLoop", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "enterAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "rideAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "exitAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleEnterAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleRideAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "vehicleExitAnimKitID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "cameraModeID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "attachmentID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "passengerAttachmentID", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "vehicleEnterAnimBone", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "vehicleExitAnimBone", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "vehicleRideAnimLoopBone", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "vehicleAbilityDisplay", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "enterUISoundID", type = Db2Type.INT),
        @Db2Field(name = "exitUISoundID", type = Db2Type.INT)
})
public class VehicleSeat implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Flags")
    private int flags;

    @Column("FlagsB")
    private int flagsB;

    @Column("FlagsC")
    private int flagsC;

    @Column("AttachmentOffsetX")
    private float attachmentOffsetX;

    @Column("AttachmentOffsetY")
    private float attachmentOffsetY;

    @Column("AttachmentOffsetZ")
    private float attachmentOffsetZ;

    @Column("EnterPreDelay")
    private float enterPreDelay;

    @Column("EnterSpeed")
    private float enterSpeed;

    @Column("EnterGravity")
    private float enterGravity;

    @Column("EnterMinDuration")
    private float enterMinDuration;

    @Column("EnterMaxDuration")
    private float enterMaxDuration;

    @Column("EnterMinArcHeight")
    private float enterMinArcHeight;

    @Column("EnterMaxArcHeight")
    private float enterMaxArcHeight;

    @Column("ExitPreDelay")
    private float exitPreDelay;

    @Column("ExitSpeed")
    private float exitSpeed;

    @Column("ExitGravity")
    private float exitGravity;

    @Column("ExitMinDuration")
    private float exitMinDuration;

    @Column("ExitMaxDuration")
    private float exitMaxDuration;

    @Column("ExitMinArcHeight")
    private float exitMinArcHeight;

    @Column("ExitMaxArcHeight")
    private float exitMaxArcHeight;

    @Column("PassengerYaw")
    private float passengerYaw;

    @Column("PassengerPitch")
    private float passengerPitch;

    @Column("PassengerRoll")
    private float passengerRoll;

    @Column("VehicleEnterAnimDelay")
    private float vehicleEnterAnimDelay;

    @Column("VehicleExitAnimDelay")
    private float vehicleExitAnimDelay;

    @Column("CameraEnteringDelay")
    private float cameraEnteringDelay;

    @Column("CameraEnteringDuration")
    private float cameraEnteringDuration;

    @Column("CameraExitingDelay")
    private float cameraExitingDelay;

    @Column("CameraExitingDuration")
    private float cameraExitingDuration;

    @Column("CameraOffsetX")
    private float cameraOffsetX;

    @Column("CameraOffsetY")
    private float cameraOffsetY;

    @Column("CameraOffsetZ")
    private float cameraOffsetZ;

    @Column("CameraPosChaseRate")
    private float cameraPosChaseRate;

    @Column("CameraFacingChaseRate")
    private float cameraFacingChaseRate;

    @Column("CameraEnteringZoom")
    private float cameraEnteringZoom;

    @Column("CameraSeatZoomMin")
    private float cameraSeatZoomMin;

    @Column("CameraSeatZoomMax")
    private float cameraSeatZoomMax;

    @Column("UiSkinFileDataID")
    private int uiSkinFileDataID;

    @Column("EnterAnimStart")
    private short enterAnimStart;

    @Column("EnterAnimLoop")
    private short enterAnimLoop;

    @Column("RideAnimStart")
    private short rideAnimStart;

    @Column("RideAnimLoop")
    private short rideAnimLoop;

    @Column("RideUpperAnimStart")
    private short rideUpperAnimStart;

    @Column("RideUpperAnimLoop")
    private short rideUpperAnimLoop;

    @Column("ExitAnimStart")
    private short exitAnimStart;

    @Column("ExitAnimLoop")
    private short exitAnimLoop;

    @Column("ExitAnimEnd")
    private short exitAnimEnd;

    @Column("VehicleEnterAnim")
    private short vehicleEnterAnim;

    @Column("VehicleExitAnim")
    private short vehicleExitAnim;

    @Column("VehicleRideAnimLoop")
    private short vehicleRideAnimLoop;

    @Column("EnterAnimKitID")
    private short enterAnimKitID;

    @Column("RideAnimKitID")
    private short rideAnimKitID;

    @Column("ExitAnimKitID")
    private short exitAnimKitID;

    @Column("VehicleEnterAnimKitID")
    private short vehicleEnterAnimKitID;

    @Column("VehicleRideAnimKitID")
    private short vehicleRideAnimKitID;

    @Column("VehicleExitAnimKitID")
    private short vehicleExitAnimKitID;

    @Column("CameraModeID")
    private short cameraModeID;

    @Column("AttachmentID")
    private byte attachmentID;

    @Column("PassengerAttachmentID")
    private byte passengerAttachmentID;

    @Column("VehicleEnterAnimBone")
    private byte vehicleEnterAnimBone;

    @Column("VehicleExitAnimBone")
    private byte vehicleExitAnimBone;

    @Column("VehicleRideAnimLoopBone")
    private byte vehicleRideAnimLoopBone;

    @Column("VehicleAbilityDisplay")
    private byte vehicleAbilityDisplay;

    @Column("EnterUISoundID")
    private int enterUISoundID;

    @Column("ExitUISoundID")
    private int exitUISoundID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
