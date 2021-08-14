package dev.the_fireplace.eer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin {
    private static final Logger LOGGER = LogManager.getLogger("eer");

    /**
     * @author The_Fireplace
     * Attempt to fix erroring entities crashing the game
     */
    @Overwrite
    public void tickEntity(Consumer<Entity> tickConsumer, Entity entity) {
        try {
            tickConsumer.accept(entity);
        } catch (Throwable throwable) {
            try {
                LOGGER.warn("Removing erroring entity at {} :", entity.getPos().toString());
                LOGGER.warn(entity.toTag(new CompoundTag()).toString());
                entity.remove();
                LOGGER.error("Erroring Entity Stacktrace:", throwable);
            } catch(Exception e) {
                CrashReport crashReport = CrashReport.create(throwable, "Ticking entity");
                CrashReportSection crashReportSection = crashReport.addElement("Entity being ticked");
                entity.populateCrashReport(crashReportSection);
                throw new CrashException(crashReport);
            }
        }
    }
}
