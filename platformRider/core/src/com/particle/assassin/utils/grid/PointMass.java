package com.particle.assassin.utils.grid;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Gayan on 7/28/2015.
 */
public class PointMass {
    public Vector3 position = new Vector3();
    public Vector3 velocity = new Vector3();
    ;
    public float inverseMass;

    private Vector3 acceleration = new Vector3();
    ;
    private float damping = 0.98f;

    public PointMass(Vector3 position, float invMass) {
        this.position = position;
        inverseMass = invMass;
    }

    public void ApplyForce(Vector3 force) {
        acceleration.mulAdd(force, inverseMass);
    }

    public void IncreaseDamping(float factor) {
        damping *= factor;
    }

    public void Update() {
        velocity.add(acceleration);
        position.add(velocity);
        acceleration.scl(0);
        if (velocity.len2() < 0.001f * 0.001f)
            velocity.scl(0);

        velocity.scl(damping);
        damping = 0.98f;
    }
}
