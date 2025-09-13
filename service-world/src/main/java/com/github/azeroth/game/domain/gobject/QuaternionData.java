package com.github.azeroth.game.domain.gobject;


import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.Position;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuaternionData {

    public float x, y, z, w;


    public boolean isUnit() {
        return Math.abs(x * x + y * y + z * z + w * w - 1.0f) < 1e-5f;
    }

    public static QuaternionData fromEulerAnglesZYX(float z, float y, float x) {
        Quaternion quaternion = new Quaternion();
        quaternion.setEulerAngles(y, x, z);
        return new QuaternionData(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public void toEulerAnglesZYX(Vector3 vector3) {
        Quaternion quaternion = new Quaternion(x, y, z, w);
        Matrix4 matrix = new Matrix4();
        matrix.set(quaternion);
        quaternion.transform(vector3);
    }
}
