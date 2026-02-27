package com.dglab.minecraft.mixin;

import com.dglab.minecraft.DamageType;
import com.dglab.minecraft.DGLabMinecraft;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    
    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && amount > 0) {
            PlayerEntity player = (PlayerEntity)(Object)this;
            
            if (player.getWorld().isClient) {
                String damageSourceName = source.getName();
                DamageType damageType = DamageType.fromDamageSource(damageSourceName);
                
                DGLabMinecraft.LOGGER.debug("Player took {} damage from {} (type: {})", 
                    amount, damageSourceName, damageType.name());
                
                DGLabMinecraft.setLastDamage(amount, damageType);
            }
        }
    }
}
