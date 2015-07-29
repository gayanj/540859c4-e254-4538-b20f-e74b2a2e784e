package com.platform.rider.utils.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.platform.rider.worldRenderer.WorldRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gayan on 7/28/2015.
 */
public class Grid {
    Spring[] springs;
    PointMass[][] points;
    TextureRegion rect;
    ShapeRenderer debugRenderer = new ShapeRenderer();

    public Grid(Rectangle size, Vector2 spacing) {
        List<Spring> springList = new ArrayList<Spring>();

        int numColumns = (int) (size.width / spacing.x) + 1;
        int numRows = (int) (size.height / spacing.y) + 1;
        points = new PointMass[numColumns][numRows];

        // these fixed points will be used to anchor the grid to fixed positions on the screen
        PointMass[][] fixedPoints = new PointMass[numColumns][numRows];

        // create the point masses
        int column = 0, row = 0;
        for (float y = size.y; y <= size.height; y += spacing.y) {
            for (float x = size.x; x <= size.width; x += spacing.x) {
                points[column][row] = new PointMass(new Vector3(x, y, 0), 1);
                fixedPoints[column][row] = new PointMass(new Vector3(x, y, 0), 0);
                //points[column][row] = new PointMass(new Vector3(x - size.width/2, y - size.height/2, 0), 1);
                //fixedPoints[column][row] = new PointMass(new Vector3(x - size.width/2, y - size.height/2, 0), 0);
                column++;
            }
            row++;
            column = 0;
        }

        // link the point masses with springs
        for (int y = 0; y < numRows; y++)
            for (int x = 0; x < numColumns; x++) {
                if (x == 0 || y == 0 || x == numColumns - 1 || y == numRows - 1)    // anchor the border of the grid
                    springList.add(new Spring(fixedPoints[x][y], points[x][y], 0.1f, 0.1f));
                else if (x % 3 == 0 && y % 3 == 0)                                    // loosely anchor 1/9th of the point masses
                    springList.add(new Spring(fixedPoints[x][y], points[x][y], 0.002f, 0.02f));

                float stiffness = 0.28f;
                float damping = 0.06f;

                if (x > 0)
                    springList.add(new Spring(points[x - 1][y], points[x][y], stiffness, damping));
                if (y > 0)
                    springList.add(new Spring(points[x][y - 1], points[x][y], stiffness, damping));
            }

        springs = springList.toArray(new Spring[springList.size()]);
    }

    public void Update() {
        for (Spring spring : springs) {
            spring.Update();
        }

        for (PointMass[] pointMasses : points) {
            for (PointMass pointMass : pointMasses) {
                pointMass.Update();
            }
        }
    }

    public void ApplyDirectedForce(Vector3 force, Vector3 position, float radius) {
        for (PointMass[] pointMasses : points) {
            for (PointMass pointMass : pointMasses) {
                if (position.dst2(pointMass.position) < radius * radius)
                    pointMass.ApplyForce(force.scl(10).scl(1 / (10 + position.dst(pointMass.position))));
            }
        }
    }

    public void ApplyImplosiveForce(float force, Vector3 position, float radius) {
        for (PointMass[] pointMasses : points) {
            for (PointMass pointMass : pointMasses) {
                float dist2 = position.dst2(pointMass.position);
                if (dist2 < radius * radius) {
                    pointMass.ApplyForce((position.sub(pointMass.position)).scl(10 * force).scl(1 / (100 + dist2)));
                    pointMass.IncreaseDamping(0.6f);
                }
            }
        }
    }

    public void ApplyImplosiveForce(float force, Vector2 position, float radius) {
        ApplyImplosiveForce(force, new Vector3(position, 0), radius);
    }

    public void ApplyExplosiveForce(float force, Vector3 position, float radius) {
        for (PointMass[] pointMasses : points) {
            for (PointMass pointMass : pointMasses) {
                float dist2 = position.dst2(pointMass.position);
                if (dist2 < radius * radius) {
                    pointMass.ApplyForce((position.sub(pointMass.position)).scl(100 * force).scl(1 / (10000 + dist2)));
                    pointMass.IncreaseDamping(0.6f);
                }
            }
        }
    }

    public void ApplyExplosiveForce(float force, Vector2 position, float radius) {
        ApplyExplosiveForce(force, new Vector3(position, 0), radius);
    }

    public Vector2 ToVec2(Vector3 v) {
        // do a perspective projection
        float factor = (v.z + 2000) / 2000;
        Vector2 screenSize = new Vector2();
        Vector2 xy = new Vector2(v.x, v.y);
        Vector2 brackets = xy.sub(new Vector2(screenSize).scl(0.5f));
        return brackets.scl(factor).add(new Vector2(screenSize).scl(0.5f));
    }

    /*void drawLine(Vector2 start, Vector2 end, float lineWidth, Color color, Matrix4 projectionMatrix) {
        //Gdx.gl.glLineWidth(lineWidth);
        debugRenderer.setProjectionMatrix(projectionMatrix);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.BLUE);
        debugRenderer.line(start, end);
        debugRenderer.end();
        //Gdx.gl.glLineWidth(1);
    }*/

    public void Draw(Matrix4 projectionMatrix) {
        int width = points.length;
        int height = points[0].length;
        Color color = new Color(30, 30, 139, 85);   // dark blue

        for (int y = 1; y < height; y++) {
            for (int x = 1; x < width; x++) {
                Vector2 left = new Vector2();
                Vector2 up = new Vector2();
                Vector2 p = ToVec2(points[x][y].position);
                if (x > 1) {
                    left = ToVec2(points[x - 1][y].position);
                    float thickness = y % 3 == 1 ? 3f : 1f;
                    WorldRenderer.DrawDebugLine(left, p, thickness, Color.BLUE, projectionMatrix);
                }
                if (y > 1) {
                    up = ToVec2(points[x][y - 1].position);
                    float thickness = x % 3 == 1 ? 3f : 1f;
                    WorldRenderer.DrawDebugLine(up, p, thickness, Color.BLUE, projectionMatrix);
                }
                if (x > 1 && y > 1) {
                    Vector2 upLeft = ToVec2(points[x - 1][y - 1].position);
                    WorldRenderer.DrawDebugLine((new Vector2(upLeft).add(up)).scl(0.5f), (new Vector2(left).add(p)).scl(0.5f), 1f, Color.BLUE, projectionMatrix);   // vertical line
                    WorldRenderer.DrawDebugLine((new Vector2(upLeft).add(left)).scl(0.5f), (new Vector2(up).add(p)).scl(0.5f), 1f, Color.BLUE, projectionMatrix);   // horizontal line
                }
            }
        }
    }
}
