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


@Table(name = "vehicle")
@Db2DataBind(name = "Vehicle.db2", layoutHash = 0x1606C582, fields = {
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true),
        @Db2Field(name = "turnSpeed", type = Db2Type.FLOAT),
        @Db2Field(name = "pitchSpeed", type = Db2Type.FLOAT),
        @Db2Field(name = "pitchMin", type = Db2Type.FLOAT),
        @Db2Field(name = "pitchMax", type = Db2Type.FLOAT),
        @Db2Field(name = "mouseLookOffsetPitch", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraFadeDistScalarMin", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraFadeDistScalarMax", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraPitchOffset", type = Db2Type.FLOAT),
        @Db2Field(name = "facingLimitRight", type = Db2Type.FLOAT),
        @Db2Field(name = "facingLimitLeft", type = Db2Type.FLOAT),
        @Db2Field(name = "cameraYawOffset", type = Db2Type.FLOAT),
        @Db2Field(name = {"seatID1", "seatID2", "seatID3", "seatID4", "seatID5", "seatID6", "seatID7", "seatID8"}, type = Db2Type.SHORT),
        @Db2Field(name = "vehicleUIIndicatorID", type = Db2Type.SHORT),
        @Db2Field(name = {"powerDisplayID1", "powerDisplayID2", "powerDisplayID3"}, type = Db2Type.SHORT),
        @Db2Field(name = "flagsB", type = Db2Type.BYTE),
        @Db2Field(name = "uiLocomotionType", type = Db2Type.BYTE),
        @Db2Field(name = "missileTargetingID", type = Db2Type.INT, signed = true)
})
public class VehicleEntry implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Flags")
    private int flags;

    @Column("TurnSpeed")
    private float turnSpeed;

    @Column("PitchSpeed")
    private float pitchSpeed;

    @Column("PitchMin")
    private float pitchMin;

    @Column("PitchMax")
    private float pitchMax;

    @Column("MouseLookOffsetPitch")
    private float mouseLookOffsetPitch;

    @Column("CameraFadeDistScalarMin")
    private float cameraFadeDistScalarMin;

    @Column("CameraFadeDistScalarMax")
    private float cameraFadeDistScalarMax;

    @Column("CameraPitchOffset")
    private float cameraPitchOffset;

    @Column("FacingLimitRight")
    private float facingLimitRight;

    @Column("FacingLimitLeft")
    private float facingLimitLeft;

    @Column("CameraYawOffset")
    private float cameraYawOffset;

    @Column("SeatID1")
    private short seatID1;

    @Column("SeatID2")
    private short seatID2;

    @Column("SeatID3")
    private short seatID3;

    @Column("SeatID4")
    private short seatID4;

    @Column("SeatID5")
    private short seatID5;

    @Column("SeatID6")
    private short seatID6;

    @Column("SeatID7")
    private short seatID7;

    @Column("SeatID8")
    private short seatID8;

    @Column("VehicleUIIndicatorID")
    private short vehicleUIIndicatorID;

    @Column("PowerDisplayID1")
    private short powerDisplayID1;

    @Column("PowerDisplayID2")
    private short powerDisplayID2;

    @Column("PowerDisplayID3")
    private short powerDisplayID3;

    @Column("FlagsB")
    private byte flagsB;

    @Column("UiLocomotionType")
    private byte uiLocomotionType;

    @Column("MissileTargetingID")
    private int missileTargetingID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
