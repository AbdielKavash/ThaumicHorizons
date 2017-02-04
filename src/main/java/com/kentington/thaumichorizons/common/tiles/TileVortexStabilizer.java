// 
// Decompiled by Procyon v0.5.30
// 

package com.kentington.thaumichorizons.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.nodes.NodeType;
import net.minecraft.util.MovingObjectPosition;
import com.kentington.thaumichorizons.client.fx.FXSonic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.api.nodes.INode;
import net.minecraft.util.Vec3;
import com.kentington.thaumichorizons.common.ThaumicHorizons;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.wands.IWandable;
import thaumcraft.api.TileThaumcraft;

public class TileVortexStabilizer extends TileThaumcraft implements IWandable
{
    public boolean hasTarget;
    public int prevType;
    public int xTarget;
    public int yTarget;
    public int zTarget;
    public TileEntity target;
    public int direction;
    boolean fireOnce;
    public boolean redstoned;
    public ForgeDirection dir;
    public Object theBeam;
    public Entity[] sonicFX;
    
    public TileVortexStabilizer() {
        this.xTarget = Integer.MAX_VALUE;
        this.yTarget = Integer.MAX_VALUE;
        this.zTarget = Integer.MAX_VALUE;
        this.target = null;
        this.fireOnce = false;
        this.theBeam = null;
        this.sonicFX = null;
    }
    
    public void updateEntity() {
        super.updateEntity();
        if (!this.fireOnce) {
            ThaumicHorizons.blockVortexStabilizer.onNeighborBlockChange(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ThaumicHorizons.blockVortexStabilizer);
            this.direction = (byte)this.getBlockMetadata();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            this.dir = ForgeDirection.getOrientation(this.direction);
            if (this.target == null) {
                this.target = this.worldObj.getTileEntity(this.xTarget, this.yTarget, this.zTarget);
            }
            this.fireOnce = true;
        }
        if (this.worldObj.getWorldTime() % 5L == 0L) {
            MovingObjectPosition mop = null;
            if (this.redstoned) {
                mop = this.worldObj.rayTraceBlocks(Vec3.createVectorHelper(this.xCoord + this.dir.offsetX + 0.75, this.yCoord + this.dir.offsetY + 0.75, this.zCoord + this.dir.offsetZ + 0.75), Vec3.createVectorHelper(this.xCoord + this.dir.offsetX * 10 + 0.5, this.yCoord + this.dir.offsetY * 10 + 0.5, this.zCoord + this.dir.offsetZ * 10 + 0.5));
            }
            if (mop != null) {
                if (mop.blockX != this.xTarget || mop.blockY != this.yTarget || mop.blockZ != this.zTarget) {
                    if (this.hasTarget) {
                        this.reHungrifyTarget();
                        this.hasTarget = false;
                    }
                    else if (!this.hasTarget && this.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ) instanceof INode) {
                        this.hasTarget = true;
                        this.target = this.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
                        this.prevType = ((INode)this.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ)).getNodeType().ordinal();
                        this.deHungrifyTarget();
                    }
                    else if (!this.hasTarget && this.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ) instanceof TileVortex) {
                        this.hasTarget = true;
                        this.target = this.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
                        this.deHungrifyTarget();
                    }
                    this.xTarget = mop.blockX;
                    this.yTarget = mop.blockY;
                    this.zTarget = mop.blockZ;
                    this.markDirty();
                    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                }
            }
            else {
                if (this.hasTarget) {
                    this.reHungrifyTarget();
                    this.hasTarget = false;
                    this.markDirty();
                    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                }
                this.xTarget = this.xCoord + this.dir.offsetX * 10;
                this.yTarget = this.yCoord + this.dir.offsetY * 10;
                this.zTarget = this.zCoord + this.dir.offsetZ * 10;
                this.target = null;
            }
        }
        if (this.worldObj.isRemote && this.redstoned && ThaumicHorizons.proxy.readyToRender() && this.xTarget != Integer.MAX_VALUE && this.yTarget != Integer.MAX_VALUE && this.zTarget != Integer.MAX_VALUE) {
            if (this.sonicFX == null) {
                this.sonicFX = new Entity[3];
            }
            for (int i = 0; i < 3; ++i) {
                if (this.sonicFX[i] == null || this.sonicFX[i].isDead) {
                    this.sonicFX[i] = (Entity)new FXSonic(Thaumcraft.proxy.getClientWorld(), this.xTarget + 0.5, this.yTarget + 0.5, this.zTarget + 0.5, 10, this.direction);
                    ThaumicHorizons.proxy.addEffect(this.sonicFX[i]);
                    break;
                }
            }
            this.theBeam = Thaumcraft.proxy.beamBore(this.worldObj, this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, this.xTarget + 0.5 - this.dir.offsetX, this.yTarget + 0.5 - this.dir.offsetY, this.zTarget + 0.5 - this.dir.offsetZ, 1, 33023, false, 2.0f, this.theBeam, 1);
        }
        else if (this.sonicFX != null) {
            for (int i = 0; i < 3; ++i) {
                if (this.sonicFX[i] != null) {
                    this.sonicFX[i].setDead();
                    this.sonicFX[i] = null;
                }
            }
        }
    }
    
    public void reHungrifyTarget() {
        if (this.target instanceof INode) {
            ((INode)this.target).setNodeType(NodeType.values()[this.prevType]);
        }
        else if (this.target instanceof TileVortex) {
            final TileVortex tileVortex = (TileVortex)this.target;
            --tileVortex.beams;
        }
        if (this.target != null) {
            this.target.markDirty();
            this.worldObj.markBlockForUpdate(this.target.xCoord, this.target.yCoord, this.target.zCoord);
        }
    }
    
    void deHungrifyTarget() {
        if (this.target instanceof INode) {
            ((INode)this.target).setNodeType(NodeType.NORMAL);
        }
        else if (this.target instanceof TileVortex) {
            final TileVortex tileVortex = (TileVortex)this.target;
            ++tileVortex.beams;
        }
        if (this.target != null) {
            this.target.markDirty();
            this.worldObj.markBlockForUpdate(this.target.xCoord, this.target.yCoord, this.target.zCoord);
        }
    }
    
    @Override
    public void writeCustomNBT(final NBTTagCompound nbttagcompound) {
        super.writeCustomNBT(nbttagcompound);
        nbttagcompound.setInteger("xT", this.xTarget);
        nbttagcompound.setInteger("yT", this.yTarget);
        nbttagcompound.setInteger("zT", this.zTarget);
        nbttagcompound.setInteger("direction", this.direction);
        nbttagcompound.setBoolean("hasTarget", this.hasTarget);
        nbttagcompound.setBoolean("active", this.redstoned);
        nbttagcompound.setInteger("prevType", this.prevType);
    }
    
    @Override
    public void readCustomNBT(final NBTTagCompound nbttagcompound) {
        super.readCustomNBT(nbttagcompound);
        this.xTarget = nbttagcompound.getInteger("xT");
        this.yTarget = nbttagcompound.getInteger("yT");
        this.zTarget = nbttagcompound.getInteger("zT");
        this.direction = nbttagcompound.getInteger("direction");
        this.hasTarget = nbttagcompound.getBoolean("hasTarget");
        this.redstoned = nbttagcompound.getBoolean("active");
        this.prevType = nbttagcompound.getInteger("prevType");
    }
    
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return TileVortexStabilizer.INFINITE_EXTENT_AABB;
    }
    
    @Override
    public int onWandRightClick(final World world, final ItemStack wandstack, final EntityPlayer player, final int x, final int y, final int z, final int side, final int md) {
        this.dir = ForgeDirection.getOrientation(side);
        world.setBlockMetadataWithNotify(x, y, z, this.direction = side, 3);
        player.worldObj.playSound(x + 0.5, y + 0.5, z + 0.5, "thaumcraft:tool", 0.5f, 0.9f + player.worldObj.rand.nextFloat() * 0.2f, false);
        player.swingItem();
        this.markDirty();
        return 0;
    }
    
    @Override
    public ItemStack onWandRightClick(final World world, final ItemStack wandstack, final EntityPlayer player) {
        return null;
    }
    
    @Override
    public void onUsingWandTick(final ItemStack wandstack, final EntityPlayer player, final int count) {
    }
    
    @Override
    public void onWandStoppedUsing(final ItemStack wandstack, final World world, final EntityPlayer player, final int count) {
    }
}
