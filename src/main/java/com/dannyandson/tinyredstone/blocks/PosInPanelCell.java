package com.dannyandson.tinyredstone.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PosInPanelCell extends PanelCellPos {
    private final double x;
    private final double z;
    private final double y;

    protected PosInPanelCell(PanelTile panelTile, int row, int level, int cell, double x, double y, double z) {
        super(panelTile,row, cell,level);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public PanelCellSegment getSegment() {
        int segmentRow = Math.round((float)(this.x*3f)-0.5f);
        int segmentColumn = Math.round((float)(this.z*3f)-0.5f);

        if(segmentRow == 0) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.FRONT_RIGHT;
                case 1: return PanelCellSegment.FRONT;
                case 2: return PanelCellSegment.FRONT_LEFT;
            }
        } else if(segmentRow == 1) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.RIGHT;
                case 1: return PanelCellSegment.CENTER;
                case 2: return PanelCellSegment.LEFT;
            }
        } else if(segmentRow == 2) {
            switch (segmentColumn) {
                case 0: return PanelCellSegment.BACK_RIGHT;
                case 1: return PanelCellSegment.BACK;
                case 2: return PanelCellSegment.BACK_LEFT;
            }
        }
        return null;
    }

    public static PosInPanelCell fromCoordinates(PanelTile panelTile, PanelCellPos panelCellPos, double x, double y, double z) {
        if(x >= 0.0 && x <= 1.0 && z >= 0.0 && z <= 1.0) {
            double rotatedX;
            double rotatedZ;

            Side direction = panelCellPos.getCellFacing();

            if (direction == Side.FRONT) {
                rotatedX = z;
                rotatedZ = 1.0 - x;
            } else if (direction == Side.RIGHT) {
                rotatedX = 1.0 - x;
                rotatedZ = 1.0 - z;
            } else if (direction == Side.BACK){
                rotatedX = 1.0 - z;
                rotatedZ = x;
            } else {
                rotatedX = x;
                rotatedZ = z;
            }

            return new PosInPanelCell(panelTile, panelCellPos.getRow(),panelCellPos.getLevel(), panelCellPos.getColumn(), rotatedX,y, rotatedZ);
        }
        return null;
    }

    public static PosInPanelCell fromHitVec(PanelTile panelTile, BlockPos pos, BlockHitResult result) {

        Direction panelFacing = panelTile.getBlockState().getValue(BlockStateProperties.FACING);
        Direction rayTraceDirection = result.getDirection().getOpposite();
        Vec3 hitVec;
        if (pos.equals(result.getBlockPos()))
            hitVec = result.getLocation().add((double)rayTraceDirection.getStepX()*.001d,(double)rayTraceDirection.getStepY()*.001d,(double)rayTraceDirection.getStepZ()*.001d);
        else
            hitVec=result.getLocation();

        double relX,relY,relZ;

        if (panelFacing==Direction.NORTH) {
            relX = hitVec.x - pos.getX();
            relY = hitVec.z-pos.getZ();
            relZ = 1 - (hitVec.y - pos.getY());
        }
        else if (panelFacing==Direction.EAST) {
            relX = hitVec.y - pos.getY();
            relY = 1-(hitVec.x - pos.getX());
            relZ = hitVec.z - pos.getZ();
        }
        else if (panelFacing==Direction.SOUTH) {
            relX = hitVec.x - pos.getX();
            relY = 1-(hitVec.z-pos.getZ());
            relZ = hitVec.y - pos.getY();
        }
        else if (panelFacing==Direction.WEST) {
            relX =1-(hitVec.y - pos.getY());
            relY = hitVec.x-pos.getX();
            relZ = hitVec.z - pos.getZ();
        }
        else if (panelFacing==Direction.UP) {
            relX = hitVec.x - pos.getX();
            relY = 1 - (hitVec.y - pos.getY());
            relZ = 1 - (hitVec.z - pos.getZ());
        }
        else{
            relX = hitVec.x - pos.getX();
            relZ = hitVec.z - pos.getZ();
            relY = hitVec.y - pos.getY();
        }


        if (panelTile.hasBase() && relY<.125 && relY>.0625)
            relY+=.002f;
        if (relX==1.0)relX=.99;
        if (relZ==1.0)relZ=.99;
        if (relY==1.0)relY=.99;

        PanelCellPos panelCellPos = PanelCellPos.fromCoordinates(panelTile, relX, relY,relZ);
        if(panelCellPos == null) return null;

        relX = (relX - (panelCellPos.getRow()/8d))*8d;
        relY = ((relY-1f/8f) - (panelCellPos.getLevel()/8d))*8d;
        relZ = (relZ - (panelCellPos.getColumn()/8d))*8d;

        return fromCoordinates(panelTile, panelCellPos, relX,relY,relZ);
    }
}
