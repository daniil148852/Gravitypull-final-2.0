package com.gravitygauntlet.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class GravityGauntletItem extends Item {
	
	public GravityGauntletItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<net.minecraft.item.ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!world.isClient) {
			ServerWorld serverWorld = (ServerWorld) world;
			
			// Если игрок присел - выстрелить всеми блоками
			if (player.isSneaking()) {
				shootAllBlocks(serverWorld, player);
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			
			// Иначе - захватить блок (используем переименованный метод customRaycast)
			BlockHitResult hitResult = customRaycast(world, player, RaycastContext.FluidHandling.NONE);
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				BlockPos pos = hitResult.getBlockPos();
				BlockState state = world.getBlockState(pos);
				
				// Проверяем, что это не воздух и не жидкость
				if (!state.isAir() && state.getFluidState().isEmpty()) {
					// Создаем падающий блок
					FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(serverWorld, pos, state);
					
					// Сохраняем UUID владельца в NBT
					NbtCompound nbt = new NbtCompound();
					fallingBlock.writeNbt(nbt);
					nbt.putUuid("GravityOwner", player.getUuid());
					nbt.putLong("GravityTime", world.getTime());
					fallingBlock.readNbt(nbt);
					
					// Отключаем урон от падения и гравитацию
					fallingBlock.dropItem = false;
					fallingBlock.setNoGravity(true);
					
					// Удаляем оригинальный блок
					world.removeBlock(pos, false);
					
					return TypedActionResult.success(player.getStackInHand(hand));
				}
			}
		}
		
		return TypedActionResult.pass(player.getStackInHand(hand));
	}
	
	// ПЕРЕИМЕНОВАНО: Item.raycast является static в ванильном коде, поэтому используем другое имя
	private BlockHitResult customRaycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
		Vec3d start = player.getCameraPosVec(1.0F);
		Vec3d direction = player.getRotationVec(1.0F);
		Vec3d end = start.add(direction.multiply(5.0));
		
		return world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
	}
	
	private void shootAllBlocks(ServerWorld world, PlayerEntity player) {
		Vec3d lookVec = player.getRotationVec(1.0F);
		
		world.getEntitiesByClass(FallingBlockEntity.class, player.getBoundingBox().expand(10.0), 
			entity -> {
				NbtCompound nbt = new NbtCompound();
				entity.writeNbt(nbt);
				if (nbt.containsUuid("GravityOwner")) {
					return nbt.getUuid("GravityOwner").equals(player.getUuid());
				}
				return false;
			}).forEach(entity -> {
				// Включаем гравитацию
				entity.setNoGravity(false);
				
				// Выстреливаем
				entity.setVelocity(lookVec.multiply(2.0));
				entity.velocityModified = true;
				
				// Помечаем как выстреленный
				NbtCompound nbt = new NbtCompound();
				entity.writeNbt(nbt);
				nbt.putBoolean("GravityShot", true);
				entity.readNbt(nbt);
			});
	}
}
