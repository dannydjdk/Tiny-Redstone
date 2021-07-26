package com.dannyandson.tinyredstone.blocks;

import com.mojang.math.Vector3d;

public class PanelCellVoxelShape {

    private Vector3d point1,point2;

    /**
     * Constructor defining a cuboid shape with 2 points
     * @param point1 vector defining point 1 of the shape (values between 0.0 and 1.0)
     * @param point2 vector defining opposite corner of the shape (values between 0.0 and 1.0)
     */
    public PanelCellVoxelShape(Vector3d point1, Vector3d point2){
        this.point1=point1;
        this.point2=point2;
    }
    public Vector3d getPoint1() {
        return point1;
    }
    public Vector3d getPoint2() {
        return point2;
    }

    /**
     * Tiny component that fills the entire cell
     */
    public static PanelCellVoxelShape FULLCELL = new PanelCellVoxelShape(new Vector3d(0d,0d,0d),new Vector3d(1d,1d,1d));
    public static PanelCellVoxelShape QUARTERCELLSLAB = new PanelCellVoxelShape(new Vector3d(0d,0d,0d),new Vector3d(1d,0.25d,1d));
    public static PanelCellVoxelShape BUTTONSHAPE = new PanelCellVoxelShape(new Vector3d(0.25d,0d,0.25d),new Vector3d(.75d,0.25d,.75d));

}
