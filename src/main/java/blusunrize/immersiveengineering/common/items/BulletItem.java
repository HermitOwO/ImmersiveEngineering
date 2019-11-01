/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotFlareEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotHomingEntity;
import blusunrize.immersiveengineering.common.entities.WolfpackShotEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BulletItem extends IEBaseItem implements ITextureOverride
{
	//TODO separate items for each bullet type?
	public BulletItem()
	{
		super("bullet", new Properties());
	}

	public static void initBullets()
	{
		BulletHandler.registerBullet("casull", new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeCasullDamage((RevolvershotEntity)projectile, shooter),
				IEConfig.TOOLS.bulletDamage_Casull.get().floatValue(),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:item/bullet_casull")));

		BulletHandler.registerBullet("armor_piercing", new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causePiercingDamage((RevolvershotEntity)projectile, shooter),
				IEConfig.TOOLS.bulletDamage_AP.get().floatValue(),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:item/bullet_armor_piercing")));

		BulletHandler.registerBullet("buckshot", new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeBuckshotDamage((RevolvershotEntity)projectile, shooter),
				IEConfig.TOOLS.bulletDamage_Buck.get().floatValue(),
				true,
				false,
				BulletHandler.emptyShell,
				new ResourceLocation("immersiveengineering:item/bullet_buckshot"))
		{
			@Override
			public int getProjectileCount(PlayerEntity shooter)
			{
				return 10;
			}
		});

		BulletHandler.registerBullet("he", new BulletHandler.DamagingBullet(null, 0, BulletHandler.emptyCasing, new ResourceLocation("immersiveengineering:item/bullet_he"))
		{
			@Override
			public void onHitTarget(World world, RayTraceResult target, UUID shooter, Entity projectile, boolean headshot)
			{
				world.createExplosion(world.getPlayerByUuid(shooter), projectile.posX, projectile.posY, projectile.posZ, 2, Mode.BREAK);
			}

			@Override
			public Entity getProjectile(@Nullable PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean charged)
			{
				if(projectile instanceof RevolvershotEntity)
				{
					((RevolvershotEntity)projectile).setGravity(0.05f);
					((RevolvershotEntity)projectile).setMovementDecay(0.9f);
				}
				return projectile;
			}

			@Override
			public SoundEvent getSound()
			{
				return IESounds.revolverFireThump;
			}
		});

		BulletHandler.registerBullet("silver", new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeSilverDamage((RevolvershotEntity)projectile, shooter),
				IEConfig.TOOLS.bulletDamage_Silver.get().floatValue(),
				BulletHandler.emptyCasing,
				new ResourceLocation("immersiveengineering:item/bullet_silver")));

		BulletHandler.registerBullet("dragonsbreath", new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeDragonsbreathDamage((RevolvershotEntity)projectile, shooter),
				IEConfig.TOOLS.bulletDamage_Dragon.get().floatValue(),
				true,
				true,
				BulletHandler.emptyShell,
				new ResourceLocation("immersiveengineering:item/bullet_dragonsbreath"))
		{
			@Override
			public int getProjectileCount(PlayerEntity shooter)
			{
				return 30;
			}

			@Override
			public Entity getProjectile(PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean electro)
			{
				((RevolvershotEntity)projectile).setTickLimit(10);
				projectile.setFire(3);
				return projectile;
			}
		});

		BulletHandler.registerBullet("potion", new PotionBullet());

		BulletHandler.registerBullet("flare", new FlareBullet());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillItemGroup(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> list)
	{
		if(this.isInGroup(tab))
		{
			for(Map.Entry<String, IBullet> entry : BulletHandler.registry.entrySet())
				if(entry.getValue().isProperCartridge())
				{
					ItemStack s = new ItemStack(this, 1);
					ItemNBTHelper.putString(s, "bullet", entry.getKey());
					list.add(s);
				}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
			String key = ItemNBTHelper.getString(stack, "bullet");
			IBullet bullet = BulletHandler.getBullet(key);
			if(bullet!=null)
				bullet.addTooltip(stack, world, list, flag);
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(@Nonnull ItemStack stack)
	{
		String s = "item.immersiveengineering.bullet.";
		String key = ItemNBTHelper.getString(stack, "bullet");
		s += key;
		IBullet bullet = BulletHandler.getBullet(key);
		if(bullet!=null)
			s = bullet.getTranslationKey(stack, s);
		return new TranslationTextComponent(s+".name");
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		if(ItemNBTHelper.hasKey(stack, "bullet"))
		{
			IBullet bullet = BulletHandler.getBullet(ItemNBTHelper.getString(stack, "bullet"));
			if(bullet!=null)
				return bullet.getColour(stack, pass);
		}
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getModelCacheKey(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "bullet"))
			return ItemNBTHelper.getString(stack, "bullet");
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		IBullet bullet = BulletHandler.getBullet(key);
		if(bullet!=null)
			return ImmutableList.copyOf(bullet.getTextures());
		return ImmutableList.of(new ResourceLocation("immersiveengieering:items/bullet_casull"));
	}

	public static class PotionBullet extends BulletHandler.DamagingBullet
	{
		public PotionBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causePotionDamage((RevolvershotEntity)projectile, shooter),
					IEConfig.TOOLS.bulletDamage_Potion.get().floatValue(),
					BulletHandler.emptyCasing,
					new ResourceLocation("immersiveengineering:item/bullet_potion"), new ResourceLocation("immersiveengineering:item/bullet_potion_layer"));
		}

		@Override
		public String getTranslationKey(ItemStack cartridge, String baseName)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(cartridge, "potion");
			if(!pot.isEmpty())
				if(pot.getItem() instanceof LingeringPotionItem)
					baseName += ".linger";
				else if(pot.getItem() instanceof SplashPotionItem)
					baseName += ".splash";
			return baseName;
		}

		@Override
		public Entity getProjectile(PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			((RevolvershotEntity)projectile).bulletPotion = ItemNBTHelper.getItemStack(cartridge, "potion");
			return projectile;
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, UUID shooter, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooter, projectile, headshot);
			RevolvershotEntity bullet = (RevolvershotEntity)projectile;
			if(!bullet.bulletPotion.isEmpty()&&bullet.bulletPotion.hasTag())
			{
				Potion potionType = PotionUtils.getPotionFromItem(bullet.bulletPotion);
				List<EffectInstance> effects = PotionUtils.getEffectsFromStack(bullet.bulletPotion);
				if(effects!=null)
					if(bullet.bulletPotion.getItem() instanceof LingeringPotionItem)
					{
						AreaEffectCloudEntity entityareaeffectcloud = new AreaEffectCloudEntity(bullet.world, bullet.posX, bullet.posY, bullet.posZ);
						entityareaeffectcloud.setOwner(world.getPlayerByUuid(shooter));
						entityareaeffectcloud.setRadius(3.0F);
						entityareaeffectcloud.setRadiusOnUse(-0.5F);
						entityareaeffectcloud.setWaitTime(10);
						entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius()/(float)entityareaeffectcloud.getDuration());
						entityareaeffectcloud.setPotion(potionType);
						for(EffectInstance potioneffect : effects)
							entityareaeffectcloud.addEffect(new EffectInstance(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
						bullet.world.addEntity(entityareaeffectcloud);
					}
					else if(bullet.bulletPotion.getItem() instanceof SplashPotionItem)
					{
						List<LivingEntity> livingEntities = bullet.world.getEntitiesWithinAABB(LivingEntity.class, bullet.getBoundingBox().grow(4.0D, 2.0D, 4.0D));
						if(livingEntities!=null&&!livingEntities.isEmpty())
							for(LivingEntity living : livingEntities)
								if(living.canBeHitWithPotion())
								{
									double dist = bullet.getDistanceSq(living);
									if(dist < 16D)
									{
										double dist2 = 1-Math.sqrt(dist)/4D;
										if(living==((EntityRayTraceResult)target).getEntity())
											dist2 = 1D;
										for(EffectInstance p : effects)
											if(p.getPotion().isInstant())
												p.getPotion().affectEntity(bullet, world.getPlayerByUuid(shooter), living, p.getAmplifier(), dist2);
											else
											{
												int j = (int)(dist2*p.getDuration()+.5D);
												if(j > 20)
													living.addPotionEffect(new EffectInstance(p.getPotion(), j, p.getAmplifier()));
											}
									}
								}

					}
					else if(((EntityRayTraceResult)target).getEntity() instanceof LivingEntity)
						for(EffectInstance p : effects)
						{
							if(p.getDuration() < 1)
								p = new EffectInstance(p.getPotion(), 1);
							((LivingEntity)((EntityRayTraceResult)target).getEntity()).addPotionEffect(p);
						}
				world.playEvent(2002, new BlockPos(bullet), PotionUtils.getPotionColor(potionType));
			}
		}


		@Override
		public void addTooltip(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(!pot.isEmpty()&&pot.getItem() instanceof PotionItem)
				PotionUtils.addPotionTooltip(pot, list, 1f);
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer==1)
			{
				ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
				return pot.isEmpty()?0xff385dc6: PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(pot));
			}
			return 0xffffffff;
		}
	}

	public static class FlareBullet implements BulletHandler.IBullet
	{
		static ResourceLocation[] textures = {new ResourceLocation("immersiveengineering:item/bullet_flare"), new ResourceLocation("immersiveengineering:item/bullet_flare_layer")};

		public FlareBullet()
		{
		}

		@Override
		public Entity getProjectile(PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			RevolvershotFlareEntity flare = shooter!=null?new RevolvershotFlareEntity(projectile.world, shooter,
					projectile.getMotion().x*1.5,
					projectile.getMotion().y*1.5,
					projectile.getMotion().z*1.5, this, cartridge):
					new RevolvershotFlareEntity(projectile.world, projectile.posX, projectile.posY, projectile.posZ, 0, 0, 0, this);
			flare.setMotion(projectile.getMotion());
			flare.bulletElectro = electro;
			flare.colour = this.getColour(cartridge, 1);
			flare.setColourSynced();
			return flare;
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, UUID shooter, Entity projectile, boolean headshot)
		{
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return BulletHandler.emptyShell;
		}

		@Override
		public ResourceLocation[] getTextures()
		{
			return textures;
		}

		@Override
		public void addTooltip(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag)
		{
			if(stack.getItem() instanceof IColouredItem)
			{
				String hexCol = Integer.toHexString(((IColouredItem)stack.getItem()).getColourForIEItem(stack, 1));
				list.add(new TranslationTextComponent(Lib.DESC_INFO+"bullet.flareColour", "<hexcol="+hexCol+":#"+hexCol+">"));
			}
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer!=1)
				return 0xffffffff;
			return ItemNBTHelper.hasKey(stack, "flareColour")?ItemNBTHelper.getInt(stack, "flareColour"): 0xcc2e06;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}

	public static class HomingBullet extends BulletHandler.DamagingBullet
	{
		public HomingBullet(float damage, ResourceLocation... textures)
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeHomingDamage((RevolvershotEntity)projectile, shooter),
					damage,
					BulletHandler.emptyCasing,
					textures);
		}

		@Override
		public Entity getProjectile(PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			RevolvershotHomingEntity shot = shooter!=null?new RevolvershotHomingEntity(projectile.world, shooter,
					projectile.getMotion().x*1.5, projectile.getMotion().y*1.5, projectile.getMotion().z*1.5, this, cartridge): new RevolvershotHomingEntity(projectile.world, projectile.posX, projectile.posY, projectile.posZ, 0, 0, 0, this);
			shot.setMotion(projectile.getMotion());
			shot.bulletElectro = electro;
			return shot;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
					IEConfig.TOOLS.bulletDamage_Wolfpack.get().floatValue(),
					BulletHandler.emptyShell,
					new ResourceLocation("immersiveengineering:item/bullet_wolfpack"));
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, UUID shooter, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooter, projectile, headshot);
			Vec3d v = projectile.getMotion().scale(-1);
			int split = 6;
			for(int i = 0; i < split; i++)
			{
				float angle = i*(360f/split);
				Matrix4 matrix = new Matrix4();
				matrix.rotate(angle, v.x, v.y, v.z);
				Vec3d vecDir = new Vec3d(0, 1, 0);
				vecDir = matrix.apply(vecDir);

				WolfpackShotEntity bullet;
				if(shooter!=null)
					bullet = new WolfpackShotEntity(world, world.getPlayerByUuid(shooter),
							vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, this, null);
				else
					bullet = new WolfpackShotEntity(world, 0, 0, 0, 0, 0, 0, this);
				EntityRayTraceResult eTarget = (EntityRayTraceResult)target;
				if(eTarget.getEntity() instanceof LivingEntity)
					bullet.targetOverride = (LivingEntity)eTarget.getEntity();
				bullet.setPosition(eTarget.getHitVec().x+vecDir.x, eTarget.getHitVec().y+vecDir.y, eTarget.getHitVec().z+vecDir.z);
				bullet.setMotion(vecDir.scale(.375));
				world.addEntity(bullet);
			}
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackPartBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackPartBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
					IEConfig.TOOLS.bulletDamage_WolfpackPart.get().floatValue(),
					BulletHandler.emptyCasing,
					new ResourceLocation("immersiveengineering:item/bullet_wolfpack"));
		}

		@Override
		public boolean isProperCartridge()
		{
			return false;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}
}