package dev.the_fireplace.eer.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class WorldMixin {
    private static final Logger LOGGER = LogManager.getLogger("eer");

    /**
     * @author The_Fireplace
     * @reason Avoid letting erroring entities crash the game if possible
     */
    @Overwrite
    public void guardEntityTick(Consumer<Entity> tickConsumer, Entity entity) {
        try {
            tickConsumer.accept(entity);
        } catch (Throwable throwable) {
            try {
                LOGGER.warn("Removing erroring entity of type {} at {} :", Registry.ENTITY_TYPE.getKey(entity.getType()).toString(), entity.position().toString());
                LOGGER.warn(entity.saveWithoutId(new CompoundTag()).toString());
                entity.remove(Entity.RemovalReason.DISCARDED);
                LOGGER.error("Erroring Entity Stacktrace:", throwable);
            } catch (Exception e) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Ticking entity");
                CrashReportCategory crashReportSection = crashReport.addCategory("Entity being ticked");
                entity.fillCrashReportCategory(crashReportSection);
                throw new ReportedException(crashReport);
            }
        }
    }
}
