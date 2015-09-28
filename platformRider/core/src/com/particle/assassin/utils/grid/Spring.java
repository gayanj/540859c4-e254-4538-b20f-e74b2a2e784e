package com.particle.assassin.utils.grid;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Gayan on 7/28/2015.
 */
public class Spring {
    public PointMass end1;
    public PointMass end2;
    public float targetLength;
    public float stiffness;
    public float damping;

    public Spring(PointMass end1, PointMass end2, float stiffness, float damping) {
        this.end1 = end1;
        this.end2 = end2;
        this.stiffness = stiffness;
        this.damping = damping;
        targetLength = end1.position.dst(end2.position) * 0.95f;
    }

    public void Update() {
        Vector3 x = new Vector3(end1.position).sub(end2.position);
        float length = x.len();
        // these springs can only pull, not push
        if (length <= targetLength) {
            return;
        }

        x.scl(1f / length).scl(length - targetLength);
        Vector3 dv = new Vector3(end2.velocity).sub(end1.velocity);
        Vector3 force = new Vector3(x).scl(stiffness).sub(dv.scl(damping));

        end1.ApplyForce(new Vector3(force).scl(-1));
        end2.ApplyForce(force);
    }
}
