package com.gravitygauntlet.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
	
	public FallingBlockEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		FallingBlockEntity self = (FallingBlockEntity)(Object)this;
		
		if (this.getWorld().isClient) return;
		
		NbtCompound nbt = new NbtCompound();
		self.writeNbt(nbt);
		
		if (!nbt.containsUuid("GravityOwner")) return;
		
		UUID ownerUuid = nbt.getUuid("GravityOwner");
		long startTime = nbt.getLong("GravityTime");
		boolean isShot = nbt.getBoolean("GravityShot");
		
		if (isShot) {
			List<Entity> entities = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.5));
			for (Entity entity : entities) {
				if (entity instanceof PlayerEntity && entity.getUuid().equals(ownerUuid)) {
					continue;
				}
				if (entity instanceof PlayerEntity || entity.getType().getSpawnGroup().isPeaceful()) {
					entity.damage(this.getDamageSources().fallingBlock(this), 8.0F);
					self.dropItem = true;
					self.discard();
					break;
				}
			}
			return;
		}
		
		PlayerEntity owner = this.getWorld().getPlayerByUuid(ownerUuid);
		if (owner == null || !owner.isAlive()) {
			self.setNoGravity(false);
			return;
		}
		
		double radius = 3.0;
		double time = (this.getWorld().getTime() - startTime + this.getId() * 20) * 0.05;
		
		double offsetX = Math.cos(time) * radius;
		double offsetZ = Math.sin(time) * radius;
		double offsetY = Math.sin(time * 2) * 0.5 + 2.0;
		
		Vec3d targetPos = owner.getPos().add(offsetX, offsetY, offsetZ);
		Vec3d currentPos = self.getPos();
		
		Vec3d direction = targetPos.subtract(currentPos).normalize();
		double distance = currentPos.distanceTo(targetPos);
		
		if (distance > 0.1) {
			double speed = Math.min(distance * 0.3, 0.5);
			self.setVelocity(direction.multiply(speed));
			self.velocityModified = true;
		} else {
			self.setVelocity(Vec3d.ZERO);
		}
		
		self.timeFalling = 0;
	}
}
